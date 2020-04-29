package io.jenkins.plugins.credentials.secretsmanager;

import hudson.model.Label;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import io.jenkins.plugins.credentials.secretsmanager.util.git.GitHttpServer;
import io.jenkins.plugins.credentials.secretsmanager.util.git.GitSshServer;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.security.KeyPair;
import java.util.Collections;

/**
 * The credentials provider should work with the Git plugin, a key credentials consumer
 */
@RunWith(Enclosed.class)
public class GitPluginIT  {

    public static class SSHUserPrivateKeyIT extends AbstractPluginIT {
        private final String repo = "foo";
        private final KeyPair sshKey = Crypto.newKeyPair();
        private final String username = "joe";

        @Rule
        public AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

        @Rule
        public final GitSshServer git = new GitSshServer.Builder()
                .withRepos(repo)
                .withUsers(Collections.singletonMap(username, sshKey.getPublic()))
                .build();

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldSupportGitPlugin() throws Exception {
            final String slaveName = "agent";
            r.createSlave(Label.get(slaveName));

            // Given
            final CreateSecretOperation.Result foo = secretsManager.createSshUserPrivateKeySecret(username, Crypto.save(sshKey.getPrivate()));

            // When
            final WorkflowRun run = runPipeline(Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: '" + git.getCloneUrl(repo, username) + "', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}"));

            // Then
            WorkflowRunAssert.assertThat(run)
                    .hasResult(hudson.model.Result.SUCCESS)
                    .hasLogContaining("Commit message: \"Initial commit\"");
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
            final CreateSecretOperation.Result foo = secretsManager.createUsernamePasswordSecret("agitter", "letmein");

            // When
            final WorkflowRun run = runPipeline(Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: '" + git.getCloneUrl() + "', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}"));

            // Then
            WorkflowRunAssert.assertThat(run)
                    .hasResult(hudson.model.Result.SUCCESS)
                    .hasLogContaining("Commit message: \"Initial commit\"");
        }

    }

}
