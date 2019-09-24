package io.jenkins.plugins.credentials.secretsmanager;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support the Jenkinsfile 'environment' step with credentials() binding.
 */
public class EnvironmentStepIT extends AbstractPluginIT implements CredentialTypeTests {

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportStringCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret");

        // When
        final WorkflowRunResult result = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        VAR = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"Credential: $VAR\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: ****");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportUsernamePasswordCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"Credential: {username: $FOO_USR, password: $FOO_PSW}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyCredentials() {
        Assertions.fail("TODO implement this test");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportCertificateCredentials() {
        Assertions.fail("TODO implement this test");
    }
}
