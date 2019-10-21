package io.jenkins.plugins.credentials.secretsmanager.util.git;

import com.jcraft.jsch.JSch;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jenkinsci.main.modules.cli.auth.ssh.PublicKeySignatureWriter;
import org.junit.rules.ExternalResource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GitSshServer extends ExternalResource {

    private SshTestGitServer server;

    private int port = 0;
    private final List<String> repos;
    private final Map<String, KeyPair> users;

    private GitSshServer(List<String> repos, Map<String, KeyPair> users) {
        this.repos = repos;
        this.users = users;
    }

    @Override
    protected void before() throws Throwable {
        final Path dataDir = Files.createTempDirectory("data");

        final String repoName = repos.stream().findFirst().orElseThrow(() -> new IllegalStateException("Git server must have a repo"));
        final Path repoDir = Files.createDirectories(dataDir.resolve("repos").resolve(repoName));
        final Repository repo = FileRepositoryBuilder.create(repoDir.resolve(".git").toFile());
        repo.create();

        try (Git git = new Git(repo)) {
            final Path readme = Files.createFile(repoDir.resolve("README"));
            git.add().addFilepattern(readme.getFileName().toString()).call();
            git.commit().setMessage("Initial commit").call();
        }

        final Map.Entry<String, KeyPair> user = users.entrySet().stream().findFirst().orElseThrow(() -> new IllegalStateException("Git server must have a user"));
        final String username = user.getKey();
        final KeyPair keyPair = user.getValue();

        final Path sshDir = Files.createDirectories(dataDir.resolve(".ssh"));
        final Path publicKey = save(keyPair, sshDir);

        final byte[] hostKey = createHostKey();

        this.server = new SshTestGitServer(username, publicKey, repo, hostKey);
        this.port = server.start();
    }

    @Override
    protected void after() {
        if (this.server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                // Don't care
            }
        }
    }

    private static byte[] createHostKey() throws Exception {
        JSch jsch = new JSch();
        com.jcraft.jsch.KeyPair pair = com.jcraft.jsch.KeyPair.genKeyPair(jsch, com.jcraft.jsch.KeyPair.RSA, 2048);

        ByteArrayOutputStream publicKey = new ByteArrayOutputStream();

        pair.writePublicKey(publicKey, "");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pair.writePrivateKey(out);
            out.flush();
            return out.toByteArray();
        }
    }

    private static Path save(KeyPair keyPair, Path folder) throws Exception {
        final Path privateKeyFile = Files.createFile(folder.resolve("id_test"));
        save(keyPair.getPrivate(), privateKeyFile.toFile());

        final Path publicKey = Files.createFile(folder.resolve("id_test.pub"));
        save(keyPair.getPublic(), publicKey);

        return publicKey;
    }

    private static void save(PublicKey publicKey, Path file) throws Exception {
        final String str = "ssh-rsa " + new PublicKeySignatureWriter().asString(publicKey);
        Files.write(file, str.getBytes());
    }

    private static void save(PrivateKey key, File file) throws Exception {
        try (FileWriter w = new FileWriter(file)) {
            final JcaPEMWriter writer = new JcaPEMWriter(w);
            writer.writeObject(key);
            writer.close();
        }
    }

    public String getCloneUrl(String repo, String username) {
        return String.format("ssh://%s@localhost:%d/%s.git", username, port, repo);
    }

    public static class Builder {
        private List<String> repos = Collections.emptyList();
        private Map<String, KeyPair> users = Collections.emptyMap();

        public Builder withRepos(String... repos) {
            this.repos = Arrays.asList(repos);
            return this;
        }

        public Builder withUsers(Map<String, KeyPair> users) {
            this.users = users;
            return this;
        }

        public GitSshServer build() {
            return new GitSshServer(repos, users);
        }
    }

}
