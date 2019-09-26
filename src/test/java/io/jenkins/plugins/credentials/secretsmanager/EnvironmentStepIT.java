package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;

import hudson.model.Result;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(Fixtures.SSH_PRIVATE_KEY, opts -> {
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
                "        echo \"{variable: $FOO, username: $FOO_USR, passphrase: $FOO_PSW}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****, username: ****, passphrase: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    /**
     * NOTE: Declarative pipeline (as defined in pipeline-model-definition plugin)  does not support
     * `environment` bindings for certificate credentials. It should fail if this kind of binding is
     * attempted.
     */
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportCertificateCredentials() {
        // Given
        final KeyPair keyPair = Crypto.newKeyPair();
        final Certificate cert = Crypto.newSelfSignedCertificate(keyPair);
        final KeyStore keyStore = Crypto.newKeyStore(Fixtures.EMPTY_PASSWORD);
        try {
            keyStore.setKeyEntry("test", keyPair.getPrivate(), Fixtures.EMPTY_PASSWORD, new Certificate[]{cert});
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        // And
        final CreateSecretOperation.Result foo = createSecret(Crypto.saveKeyStore(keyStore, Fixtures.EMPTY_PASSWORD));

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
                "        echo \"{variable: $FOO}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertThat(result.result).isEqualTo(Result.FAILURE);
    }
}
