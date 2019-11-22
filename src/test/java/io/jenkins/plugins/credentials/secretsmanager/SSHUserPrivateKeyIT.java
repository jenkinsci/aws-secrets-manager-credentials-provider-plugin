package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.security.KeyPair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.git.GitSshServer;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support SSH private key credentials.
 */
@RunWith(Enclosed.class)
public class SSHUserPrivateKeyIT extends AbstractPluginIT implements CredentialsTests {

    private static final Secret EMPTY_PASSPHRASE = Secret.fromString("");
    private static final String PRIVATE_KEY = Crypto.newPrivateKey();

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveName() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final ListBoxModel list = listCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(foo.getName(), foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAppearInCredentialsProvider() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<SSHUserPrivateKey> credentials = lookupCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(tuple(foo.getName(), "joe", PRIVATE_KEY, EMPTY_PASSPHRASE));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "node {",
                "  withCredentials([sshUserPrivateKey(credentialsId: '" + foo.getName() + "', keyFileVariable: 'KEYFILE', usernameVariable: 'USERNAME')]) {",
                "    echo \"Credential: {username: $USERNAME, keyFile: $KEYFILE}\"",
                "  }",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, keyFile: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "pipeline {",
                "  agent any",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $FOO, username: $FOO_USR}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****, username: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });
        // And
        final SSHUserPrivateKey before = lookupCredential(AwsCredentials.class, foo.getName());

        // When
        final SSHUserPrivateKey after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(foo.getName(), "joe", PRIVATE_KEY, EMPTY_PASSPHRASE);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotTolerateMalformattedPrivateKey() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("-----INVALID PRIVATE KEY", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final Optional<SSHUserPrivateKey> credentials =
                lookupCredentials(SSHUserPrivateKey.class).stream().findFirst();

        // Then
        assertSoftly(s -> {
            s.assertThat(credentials).isPresent();
            s.assertThat(credentials.get().getId()).as("ID").isEqualTo(foo.getName());
            s.assertThatThrownBy(() -> credentials.get().getPrivateKeys()).as("Private Keys").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    /*
     * NOTE: This is not an officially supported feature. It may change without warning in future.
     */
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAllowUsageAsStringCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(PRIVATE_KEY)));
    }

    /*
     * NOTE: This is not an officially supported feature. It may change without warning in future.
     */
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAllowUsageAsUsernamePasswordCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StandardUsernamePasswordCredentials> credentials =
                lookupCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "password")
                .containsOnly(tuple(foo.getName(), "joe", Secret.fromString(PRIVATE_KEY)));
    }

    public static class GitPluginIT extends AbstractPluginIT {

        private final String repo = "foo";
        private final KeyPair sshKey = Crypto.newKeyPair();
        private final String username = "joe";

        @Rule
        public final GitSshServer git = new GitSshServer.Builder()
                .withRepos(repo)
                .withUsers(Collections.singletonMap(username, sshKey.getPublic()))
                .build();

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldSupportGitPlugin() throws Exception {
            final String slaveName = "agent";
            final DumbSlave slave = r.createSlave(Label.get(slaveName));

            // Given
            final CreateSecretOperation.Result foo = createSecret(Crypto.save(sshKey.getPrivate()), opts -> {
                opts.tags = Collections.singletonMap("jenkins:credentials:username", username);
            });

            // When
            String pipeline = Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: '" + git.getCloneUrl(repo, username) + "', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}");
            final WorkflowRunResult result = runPipeline(pipeline);

            // Then
            assertSoftly(s -> {
                s.assertThat(result.log).as("Log").contains("Commit message: \"Initial commit\"");
                s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
            });
        }

    }
}
