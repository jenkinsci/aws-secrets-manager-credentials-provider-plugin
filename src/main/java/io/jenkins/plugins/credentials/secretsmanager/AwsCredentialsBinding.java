package io.jenkins.plugins.credentials.secretsmanager;

import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.UnbindableDir;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;

/**
 * Emulates the behavior of the official single-type credential binding classes for the multi-type
 * AWS Secrets Manager credential.
 */
public class AwsCredentialsBinding extends MultiBinding<AwsCredentials> {

    private static final Logger LOG = Logger.getLogger(AwsCredentialsBinding.class.getName());

    /**
     * A plain zero-length string ("") does not get successfully bound to the _PSW environment
     * variable, so we have to bind a nested zero-length string instead.
     */
    private static final String EMPTY_PASSPRHASE = "''";

    private final String variable;

    private String passwordVariable;
    private String passphraseVariable;
    private String usernameVariable;

    @DataBoundConstructor
    public AwsCredentialsBinding(String credentialsId, @Nonnull String variable) {
        super(credentialsId);
        this.variable = variable;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setUsernameVariable(@Nonnull String usernameVariable) {
        this.usernameVariable = usernameVariable;
    }

    @CheckForNull
    @SuppressWarnings("unused")
    public String getUsernameVariable() {
        return usernameVariable;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setPasswordVariable(@Nonnull String passwordVariable) {
        this.passwordVariable = passwordVariable;
    }

    @CheckForNull
    @SuppressWarnings("unused")
    public String getPasswordVariable() {
        return passwordVariable;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setPassphraseVariable(@Nonnull String passphraseVariable) {
        this.passphraseVariable = passphraseVariable;
    }

    @CheckForNull
    @SuppressWarnings("unused")
    public String getPassphraseVariable() {
        return passphraseVariable;
    }

    @Override
    protected Class<AwsCredentials> type() {
        return AwsCredentials.class;
    }

    @Override
    public Set<String> variables() {
        final Set<String> set = new HashSet<>();

        set.add(variable);

        if (usernameVariable != null && !usernameVariable.isEmpty()) {
            set.add(usernameVariable);
        }

        if (passphraseVariable != null && !passphraseVariable.isEmpty()) {
            set.add(passphraseVariable);
        }

        if (passwordVariable != null && !passwordVariable.isEmpty()) {
            set.add(passwordVariable);
        }

        return Collections.unmodifiableSet(set);
    }

    @Override
    public MultiEnvironment bind(@Nonnull Run<?, ?> build, FilePath workspace, Launcher launcher, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        final AwsCredentials credential = getCredentials(build);

        final Map<String, String> tags = credential.getTags();
        final String secretString = credential.getSecretValue().getSecretString();
        final CredentialsType credentialsType = detect(secretString, tags);

        return credentialsType.match(new CredentialsType.Matcher<MultiEnvironment>() {

            @Override
            public MultiEnvironment string(Secret secret) {
                LOG.fine("Binding AWS Secrets Manager secret as Secret Text");

                final Map<String, String> map = Collections.singletonMap(variable, secret.getPlainText());
                return new MultiEnvironment(map);
            }

            @Override
            public MultiEnvironment usernamePassword(String username, Secret password) {
                LOG.fine("Binding AWS Secrets Manager secret as Username with Password");

                final Map<String, String> map = new HashMap<>();

                map.put(variable, String.format("%s:%s", username, password.getPlainText()));

                if (usernameVariable != null && !usernameVariable.isEmpty()) {
                    map.put(usernameVariable, username);
                }

                if (passwordVariable != null && !passwordVariable.isEmpty()) {
                    map.put(passwordVariable, password.getPlainText());
                }

                return new MultiEnvironment(map);
            }

            @Override
            public MultiEnvironment sshUserPrivateKey(List<String> privateKeys, String username, Secret passphrase) throws IOException, InterruptedException {
                LOG.fine("Binding AWS Secrets Manager secret as SSH Private Key");

                final Map<String, String> map = new HashMap<>();

                if (passphraseVariable != null && !passphraseVariable.isEmpty()) {
                    final String p = passphrase.getPlainText();
                    if (p.isEmpty()) {
                        map.put(passphraseVariable, EMPTY_PASSPRHASE);
                    } else {
                        map.put(passphraseVariable, p);
                    }
                }

                if (usernameVariable != null && !usernameVariable.isEmpty()) {
                    map.put(usernameVariable, username);
                }

                final UnbindableDir keyDir = UnbindableDir.create(workspace);
                final FilePath keyFile = keyDir.getDirPath().child("ssh-key-" + variable);
                final StringBuilder contents = new StringBuilder();
                for (String key : privateKeys) {
                    contents.append(key);
                    contents.append('\n');
                }
                keyFile.write(contents.toString(), "UTF-8");
                keyFile.chmod(0400);
                map.put(variable, keyFile.getRemote());

                return new MultiEnvironment(map);
            }
        });
    }

    private static CredentialsType detect(String secretString, Map<String, String> tags) {
        final String USERNAME_TAG = "jenkins:credentials:username";

        if (tags.containsKey(USERNAME_TAG)) {
            final String username = tags.get(USERNAME_TAG);

            if (SSHKeyValidator.isValid(secretString)) {
                return CredentialsType.sshUserPrivateKey(Collections.singletonList(secretString), username, Secret.fromString(""));
            } else {
                return CredentialsType.usernamePassword(username, Secret.fromString(secretString));
            }
        } else {
            return CredentialsType.string(Secret.fromString(secretString));
        }
    }

    /**
     * The plugin should work without pipeline-model-definition or credentials-binding plugins being
     * installed.
     */
    @Extension(optional = true)
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BindingDescriptor<AwsCredentials> {

        @Override
        protected Class<AwsCredentials> type() {
            return AwsCredentials.class;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.awsSecretsManagerSecret();
        }

        @Override
        public boolean requiresWorkspace() {
            return false;
        }
    }

    /**
     * An Algebraic Data Type to transform a multi-type Jenkins Credential into its most appropriate
     * single-type representation.
     * */
    private abstract static class CredentialsType {

        static CredentialsType string(Secret secret) {
            return new StringHolder(secret);
        }

        static CredentialsType usernamePassword(String username, Secret password) {
            return new UsernamePasswordHolder(username, password);
        }

        static CredentialsType sshUserPrivateKey(List<String> privateKeys, String username, Secret passphrase) {
            return new SshUserPrivateKeyHolder(passphrase, privateKeys, username);
        }

        abstract <R> R match(Matcher<R> matcher) throws IOException, InterruptedException;

        interface Matcher<R> {
            R string(Secret secret);

            R usernamePassword(String username, Secret password);

            R sshUserPrivateKey(List<String> privateKeys, String username, Secret passphrase) throws IOException, InterruptedException;
        }

        private static class StringHolder extends CredentialsType {
            private final Secret secret;

            private StringHolder(Secret secret) {
                this.secret = secret;
            }

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.string(secret);
            }
        }

        private static class UsernamePasswordHolder extends CredentialsType {
            private final String username;
            private final Secret password;

            private UsernamePasswordHolder(String username, Secret password) {
                this.username = username;
                this.password = password;
            }

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.usernamePassword(username, password);
            }
        }

        private static class SshUserPrivateKeyHolder extends CredentialsType {
            private final Secret passphrase;
            private final List<String> privateKeys;
            private final String username;

            private SshUserPrivateKeyHolder(Secret passphrase, List<String> privateKeys, String username) {
                this.passphrase = passphrase;
                this.privateKeys = privateKeys;
                this.username = username;
            }

            @Override
            <R> R match(Matcher<R> matcher) throws IOException, InterruptedException {
                return matcher.sshUserPrivateKey(privateKeys, username, passphrase);
            }
        }
    }
}
