package io.jenkins.plugins.credentials.secretsmanager.util.git;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ServerAuthenticationManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.gss.GSSAuthenticator;
import org.apache.sshd.server.auth.gss.UserAuthGSS;
import org.apache.sshd.server.auth.gss.UserAuthGSSFactory;
import org.apache.sshd.server.scp.UnknownCommand;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UploadPack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A test utility imported from a newer version of JGit.
 *
 * See https://github.com/eclipse/jgit/blob/v5.2.0.201811281532-m3/org.eclipse.jgit.junit.ssh/src/org/eclipse/jgit/junit/ssh/SshTestGitServer.java
 */
class SshTestGitServer {

    @NonNull
    private final String testUser;

    @NonNull
    private final Repository repository;

    @NonNull
    private final List<KeyPair> hostKeys = new ArrayList<>();

    private final SshServer server;

    @NonNull
    private PublicKey testKey;

    private final ExecutorService executorService = Executors
            .newFixedThreadPool(2);

    /**
     * Creates a ssh git <em>test</em> server. It serves one single repository,
     * and accepts public-key authentication for exactly one test user.
     *
     * @param testUser
     *            user name of the test user
     * @param testKey
     *            <em>private</em> key file of the test user; the server will
     *            only user the public key from it
     * @param repository
     *            to serve
     * @param hostKey
     *            the unencrypted private key to use as host key
     */
    SshTestGitServer(@NonNull String testUser, @NonNull Path testKey,
                     @NonNull Repository repository, @NonNull byte[] hostKey)
            throws IOException, GeneralSecurityException {
        this.testUser = testUser;
        setTestUserPublicKey(testKey);
        this.repository = repository;
        server = SshServer.setUpDefaultServer();
        // Set host key
        try (ByteArrayInputStream in = new ByteArrayInputStream(hostKey)) {
            hostKeys.add(SecurityUtils.loadKeyPairIdentity("", in, null));
        } catch (IOException | GeneralSecurityException e) {
            // Ignore.
        }
        server.setKeyPairProvider(() -> hostKeys);

        configureAuthentication();

        List<NamedFactory<Command>> subsystems = configureSubsystems();
        if (!subsystems.isEmpty()) {
            server.setSubsystemFactories(subsystems);
        }

        configureShell();

        server.setCommandFactory(command -> {
            if (command.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)) {
                return new GitUploadPackCommand(command, executorService);
            } else if (command.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
                return new GitReceivePackCommand(command, executorService);
            }
            return new UnknownCommand(command);
        });
    }

    private static class FakeUserAuthGSS extends UserAuthGSS {
        @Override
        protected Boolean doAuth(Buffer buffer, boolean initial)
                throws Exception {
            // We always reply that we did do this, but then we fail at the
            // first token message. That way we can test that the client-side
            // sends the correct initial request and then is skipped correctly,
            // even if it causes a GSSException if Kerberos isn't configured at
            // all.
            if (initial) {
                ServerSession session = getServerSession();
                Buffer b = session.createBuffer(
                        SshConstants.SSH_MSG_USERAUTH_INFO_REQUEST);
                b.putBytes(KRB5_MECH.getDER());
                session.writePacket(b);
                return null;
            }
            return Boolean.FALSE;
        }
    }

    private List<NamedFactory<UserAuth>> getAuthFactories() {
        List<NamedFactory<UserAuth>> authentications = new ArrayList<>();
        authentications.add(new UserAuthGSSFactory() {
            @Override
            public UserAuth create() {
                return new FakeUserAuthGSS();
            }
        });
        authentications.add(
                ServerAuthenticationManager.DEFAULT_USER_AUTH_PUBLIC_KEY_FACTORY);
        authentications.add(
                ServerAuthenticationManager.DEFAULT_USER_AUTH_KB_INTERACTIVE_FACTORY);
        authentications.add(
                ServerAuthenticationManager.DEFAULT_USER_AUTH_PASSWORD_FACTORY);
        return authentications;
    }

    /**
     * Configures the authentication mechanisms of this test server. Invoked
     * from the constructor. The default sets up public key authentication for
     * the test user, and a gssapi-with-mic authenticator that pretends to
     * support this mechanism, but that then refuses to authenticate anyone.
     */
    private void configureAuthentication() {
        server.setUserAuthFactories(getAuthFactories());
        // Disable some authentications
        server.setPasswordAuthenticator(null);
        server.setKeyboardInteractiveAuthenticator(null);
        server.setHostBasedAuthenticator(null);
        // Pretend we did gssapi-with-mic.
        server.setGSSAuthenticator(new GSSAuthenticator() {
            @Override
            public boolean validateInitialUser(ServerSession session,
                                               String user) {
                return false;
            }
        });
        // Accept only the test user/public key
        server.setPublickeyAuthenticator((userName, publicKey, session) -> {
            return SshTestGitServer.this.testUser.equals(userName) && KeyUtils
                    .compareKeys(SshTestGitServer.this.testKey, publicKey);
        });
    }

    /**
     * Configures the test server's subsystems (sftp, scp). Invoked from the
     * constructor. The default provides a simple SFTP setup with the root
     * directory as the given repository's .git directory's parent. (I.e., at
     * the directory containing the .git directory.)
     *
     * @return A possibly empty collection of subsystems.
     */
    @NonNull
    private List<NamedFactory<Command>> configureSubsystems() {
        // SFTP.
        server.setFileSystemFactory(new VirtualFileSystemFactory() {

            @Override
            protected Path computeRootDir(Session session) {
                return SshTestGitServer.this.repository.getDirectory()
                        .getParentFile().getAbsoluteFile().toPath();
            }
        });
        return Collections
                .singletonList((new SftpSubsystemFactory.Builder()).build());
    }

    /**
     * Configures shell access for the test server. The default provides no
     * shell at all.
     */
    private void configureShell() {
        // No shell
        server.setShellFactory(null);
    }

    /**
     * Starts the test server, listening on a random port.
     *
     * @return the port the server listens on; test clients should connect to
     *         that port
     */
    int start() throws IOException {
        server.start();
        return server.getPort();
    }

    /**
     * Stops the test server.
     */
    void stop() throws IOException {
        executorService.shutdownNow();
        server.stop(true);
    }

    private void setTestUserPublicKey(Path key)
            throws IOException, GeneralSecurityException {
        this.testKey = AuthorizedKeyEntry.readAuthorizedKeys(key).get(0)
                .resolvePublicKey(PublicKeyEntryResolver.IGNORING);
    }

    private class GitUploadPackCommand extends AbstractCommandSupport {

        GitUploadPackCommand(String command,
                             ExecutorService executorService) {
            super(command, executorService, false);
        }

        @Override
        public void run() {
            UploadPack uploadPack = new UploadPack(repository);
//            String gitProtocol = getEnvironment().getEnv().get("GIT_PROTOCOL");
//            if (gitProtocol != null) {
//                uploadPack.setExtraParameters(Collections.singleton(gitProtocol));
//            }
            try {
                uploadPack.upload(getInputStream(), getOutputStream(),
                        getErrorStream());
                onExit(0);
            } catch (IOException e) {
                log.warn(
                        MessageFormat.format("Could not run {0}", getCommand()),
                        e);
                onExit(-1, e.toString());
            }
        }

    }

    private class GitReceivePackCommand extends AbstractCommandSupport {

        GitReceivePackCommand(String command,
                              ExecutorService executorService) {
            super(command, executorService, false);
        }

        @Override
        public void run() {
            try {
                new ReceivePack(repository).receive(getInputStream(),
                        getOutputStream(), getErrorStream());
                onExit(0);
            } catch (IOException e) {
                log.warn(
                        MessageFormat.format("Could not run {0}", getCommand()),
                        e);
                onExit(-1, e.toString());
            }
        }

    }
}