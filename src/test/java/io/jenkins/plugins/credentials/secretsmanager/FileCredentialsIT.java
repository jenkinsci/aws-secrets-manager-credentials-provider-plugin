package io.jenkins.plugins.credentials.secretsmanager;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;

/**
 * The plugin should support secret file credentials.
 */
public class FileCredentialsIT implements CredentialsTests {

    private static final String FILENAME = "hello.txt";
    private static final byte[] CONTENT = {0x01, 0x02, 0x03};

    public MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createFileSecret(CONTENT);

        // When
        final var credential = lookup(FileCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasId(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveFileName() {
        // Given
        final var secret = createFileSecret(CONTENT);

        // When
        final var credential = lookup(FileCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasFileName(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveCustomisableFileName() {
        // Given
        final var secret = createFileSecret(CONTENT, FILENAME);

        // When
        final var credential = lookup(FileCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasFileName(FILENAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveContent() {
        // Given
        final var secret = createFileSecret(CONTENT);

        // When
        final var credential = lookup(FileCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasContent(CONTENT);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final var secret = createFileSecret(CONTENT);

        final var ours = lookup(FileCredentials.class, secret.name());

        final var theirs = new FileCredentialsImpl(null, "id", "description", "filename", SecretBytes.fromBytes(CONTENT));

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final var secret = createFileSecret(CONTENT);

        // When
        final var credentialList = jenkins.getCredentials().list(FileCredentials.class);

        // Then
        assertThat(credentialList)
                .containsOption(secret.name(), secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final var secret = createFileSecret(CONTENT);

        // When
        final var run = runPipeline("",
                "node {",
                "  withCredentials([file(credentialsId: '" + secret.name() + "', variable: 'FILE')]) {",
                "    echo \"Credential: {fileName: $FILE}\"",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {fileName: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final var secret = createFileSecret(CONTENT);

        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent any",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        VAR = credentials('" + secret.name() + "')",
                "      }",
                "      steps {",
                "        echo \"{filename: $VAR}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{filename: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretResponse foo = createFileSecret(CONTENT);
        final FileCredentials before = lookup(FileCredentials.class, foo.name());

        // When
        final FileCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertThat(after)
                .hasFileName(before.getFileName())
                .hasContent(getContent(before))
                .hasId(before.getId());
    }

    private static InputStream getContent(FileCredentials credentials) {
        try {
            return credentials.getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CreateSecretResponse createFileSecret(byte[] content) {
        final var tags = List.of(AwsTags.type(Type.file));

        return createSecret(content,tags);
    }

    private CreateSecretResponse createFileSecret(byte[] content, String filename) {
        final var tags = List.of(AwsTags.type(Type.file), AwsTags.filename(filename));

        return createSecret(content, tags);
    }

    private CreateSecretResponse createSecret(byte[] content, List<Tag> tags) {
        return secretsManager.getClient().createSecret((b) -> {
            b.name(CredentialNames.random());
            b.secretBinary(SdkBytes.fromByteArray(content));
            b.tags(tags);
        });
    }

    private <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return jenkins.getCredentials().lookup(type, id);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
