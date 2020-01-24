package io.jenkins.plugins.credentials.secretsmanager;

import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Secret Text credentials.
 */
public class StringCredentialsIT extends AbstractPluginIT implements CredentialsTests {
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveName() {
        // Given
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");

        // When
        final ListBoxModel list = listCredentials(StringCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(foo.getName(), foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAppearInCredentialsProvider() {
        // Given
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "withCredentials([string(credentialsId: '" + foo.getName() + "', variable: 'VAR')]) {",
                "  echo \"Credential: $VAR\"",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: ****");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        VAR = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $VAR}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");
        // And
        final StringCredentials before = lookupCredential(StringCredentials.class, foo.getName());

        // When
        final StringCredentials after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "secret")
                .containsOnly(foo.getName(), Secret.fromString("supersecret"));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveIcon() {
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");
        final StringCredentials ours = lookupCredential(StringCredentials.class, foo.getName());

        final StringCredentials theirs = new StringCredentialsImpl(null, "id", "description", Secret.fromString("secret"));

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName());
    }
}
