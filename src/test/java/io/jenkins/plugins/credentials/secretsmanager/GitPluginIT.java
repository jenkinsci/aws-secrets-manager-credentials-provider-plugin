package io.jenkins.plugins.credentials.secretsmanager;

import hudson.model.Label;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
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

    public static class SSHUserPrivateKeyIT {
        private final String repo = "foo";
        private final KeyPair sshKey = Crypto.newKeyPair();
        private final String username = "joe";

        @Rule
        public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();

        @Rule
        public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

        @Rule
        public final GitSshServer git = new GitSshServer.Builder()
                .withRepos(repo)
                .withUsers(Collections.singletonMap(username, sshKey.getPublic()))
                .build();

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldSupportGitPlugin() throws Exception {
            final String slaveName = "agent";
            jenkins.createSlave(Label.get(slaveName));

            // Given
            final CreateSecretOperation.Result foo = secretsManager.createSshUserPrivateKeySecret(username, Crypto.save(sshKey.getPrivate()));

            // When
            final WorkflowRun run = jenkins.getPipelines().run(Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: '" + git.getCloneUrl(repo, username) + "', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}"));

            // Then
            WorkflowRunAssert.assertThat(run)
                    .hasResult(hudson.model.Result.SUCCESS)
                    .hasLogContaining("Commit message: \"Initial commit\"");
        }
    }

    public static class StandardUsernamePasswordCredentialsIT {

        @Rule
        public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();

        @Rule
        public final GitHttpServer git = new GitHttpServer();

        @Rule
        public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldSupportGitPlugin() throws Exception {
            final String slaveName = "agent";
            jenkins.createSlave(Label.get(slaveName));

            // Given
            final CreateSecretOperation.Result foo = secretsManager.createUsernamePasswordSecret("agitter", "letmein");

            // When
            final WorkflowRun run = jenkins.getPipelines().run(Strings.m("",
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
