package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Username With Password credentials.
 */
public class StandardUsernamePasswordCredentialsIT extends AbstractPluginIT implements CredentialsTests {

    private static final String USERNAME = "joe";
    private static final String PASSWORD = "supersecret";

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final ListBoxModel list = listCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(USERNAME + "/******", foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePassword() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final StandardUsernamePasswordCredentials credential =
                lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        // Then
        assertThat(credential.getPassword()).isEqualTo(Secret.fromString(PASSWORD));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsername() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final StandardUsernamePasswordCredentials credential =
                lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        // Then
        assertThat(credential.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final StandardUsernamePasswordCredentials credential =
                lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        // Then
        assertThat(credential.getId()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final WorkflowRun run = runPipeline(Strings.m("",
                "withCredentials([usernamePassword(credentialsId: '" + foo.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final WorkflowRun run = runPipeline(Strings.m("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $FOO, username: $FOO_USR, password: $FOO_PSW}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****, username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);
        final StandardUsernamePasswordCredentials before = lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        // When
        final StandardUsernamePasswordCredentials after = snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getUsername()).as("Username").isEqualTo(before.getUsername());
            s.assertThat(after.getPassword()).as("Password").isEqualTo(before.getPassword());
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);
        final StandardUsernamePasswordCredentials ours = lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        final StandardUsernamePasswordCredentials theirs = new UsernamePasswordCredentialsImpl(null, "id", "description", "username", "password");

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName());
    }
}
