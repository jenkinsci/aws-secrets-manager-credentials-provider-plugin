package io.jenkins.plugins.credentials.secretsmanager;

import hudson.model.Label;
import hudson.slaves.DumbSlave;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import io.jenkins.plugins.credentials.secretsmanager.util.git.GitHttpServer;
import io.jenkins.plugins.credentials.secretsmanager.util.git.GitSshServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.security.KeyPair;
import java.util.Collections;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The credentials provider should interoperate with the Git plugin, a key credentials consumer
 */
@RunWith(Enclosed.class)
public class GitPluginIT  {

    public static class SSHUserPrivateKeyIT extends AbstractPluginIT {
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
            final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(username, Crypto.save(sshKey.getPrivate()));

            // When
            String pipeline = Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: '" + git.getCloneUrl(repo, username) + "', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}");
            final WorkflowRunResult result = runPipeline(pipeline);

            // Then
            assertSoftly(s -> {
                s.assertThat(result.log).as("Log").contains("Commit message: \"Initial commit\"");
                s.assertThat(result.log).as("Log using credential").contains("using credential " + foo.getName());
                s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
            });
        }
    }

    public static class StandardUsernamePasswordCredentialsIT extends AbstractPluginIT {

        @Rule
        public final GitHttpServer git = new GitHttpServer();

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldSupportGitPlugin() throws Exception {
            final String slaveName = "agent";
            r.createSlave(Label.get(slaveName));

            // Given
            final CreateSecretOperation.Result foo = createUsernamePasswordSecret("agitter", "letmein");

            // When
            String pipeline = Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: '" + git.getCloneUrl() + "', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}");
            final WorkflowRunResult result = runPipeline(pipeline);

            // Then
            assertSoftly(s -> {
                s.assertThat(result.log).as("Log").contains("Commit message: \"Initial commit\"");
                s.assertThat(result.log).as("Log using credential").contains("using credential " + foo.getName());
                s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
            });
        }

    }

}
