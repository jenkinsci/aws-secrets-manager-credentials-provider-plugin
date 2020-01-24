package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import org.junit.Test;

import java.util.List;

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
    public void shouldHaveName() {
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
    public void shouldAppearInCredentialsProvider() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final List<StandardUsernamePasswordCredentials> credentials =
                lookupCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "password")
                .containsOnly(tuple(foo.getName(), USERNAME, Secret.fromString(PASSWORD)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "withCredentials([usernamePassword(credentialsId: '" + foo.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
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
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****, username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);
        // And
        final StandardUsernamePasswordCredentials before = lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        // When
        final StandardUsernamePasswordCredentials after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "username", "password")
                .containsOnly(foo.getName(), USERNAME, Secret.fromString(PASSWORD));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveIcon() {
        final CreateSecretOperation.Result foo = createUsernamePasswordSecret(USERNAME, PASSWORD);
        final StandardUsernamePasswordCredentials ours = lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        final StandardUsernamePasswordCredentials theirs = new UsernamePasswordCredentialsImpl(null, "id", "description", "username", "password");

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName() + "a");
    }
}
