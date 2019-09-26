package io.jenkins.plugins.credentials.secretsmanager;

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
                "        echo \"{variable: $VAR}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****}");
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
                "        echo \"{variable: $FOO, username: $FOO_USR, password: $FOO_PSW}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****, username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Ignore("Declarative Pipeline only supports SSH key bindings from version 1.3.7")
    public void shouldSupportSshPrivateKeyCredentials() {

    }

    @Ignore("Declarative Pipeline does not yet support certificate bindings")
    public void shouldSupportCertificateCredentials() {

    }
}
