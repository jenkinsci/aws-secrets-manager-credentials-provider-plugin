package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.SecretBytes;
import com.google.common.io.ByteStreams;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support secret file credentials.
 */
public class FileCredentialsIT implements CredentialsTests {

    private static final String FILENAME = "hello.txt";
    private static final byte[] CONTENT = {0x01, 0x02, 0x03};

    @Rule
    public MyJenkinsConfiguredWithCodeRule r = new MyJenkinsConfiguredWithCodeRule();

    @Rule
    public AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);

        // When
        final FileCredentials credential = r.getCredentials().lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential.getId()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveFileName() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);

        // When
        final FileCredentials credential = r.getCredentials().lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential.getFileName()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveCustomisableFileName() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(FILENAME, CONTENT);

        // When
        final FileCredentials credential = r.getCredentials().lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential.getFileName()).isEqualTo(FILENAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveContent() throws IOException {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);

        // When
        final FileCredentials credential = r.getCredentials().lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(ByteStreams.toByteArray(credential.getContent())).isEqualTo(CONTENT);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);
        final FileCredentials ours = r.getCredentials().lookup(FileCredentials.class, foo.getName());

        final FileCredentials theirs = new FileCredentialsImpl(null, "id", "description", "filename", SecretBytes.fromBytes(CONTENT));

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);

        // When
        final ListBoxModel list = r.getCredentials().list(FileCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(foo.getName(), foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);

        // When
        final WorkflowRun run = r.getPipelines().run(Strings.m("",
                "node {",
                "  withCredentials([file(credentialsId: '" + foo.getName() + "', variable: 'FILE')]) {",
                "    echo \"Credential: {fileName: $FILE}\"",
                "  }",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {fileName: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);

        // When
        final WorkflowRun run = r.getPipelines().run(Strings.m("",
                "pipeline {",
                "  agent any",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        VAR = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{filename: $VAR}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{filename: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createFileSecret(CONTENT);
        final FileCredentials before = r.getCredentials().lookup(FileCredentials.class, foo.getName());

        // When
        final FileCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getFileName()).as("Filename").isEqualTo(before.getFileName());
            s.assertThat(getContent(after)).as("Content").hasSameContentAs(getContent(before));
        });
    }

    private static InputStream getContent(FileCredentials credentials) {
        try {
            return credentials.getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
