package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;

/**
 * The plugin should support secret file credentials.
 */
public class FileCredentialsIT implements CredentialsTests {

    private static final String FILENAME = "hello.txt";
    private static final byte[] CONTENT = {0x01, 0x02, 0x03};

    public MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins)
            .around(secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final CreateSecretResult foo = createFileSecret(CONTENT);

        // When
        final FileCredentials credential = lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasId(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveFileName() {
        // Given
        final CreateSecretResult foo = createFileSecret(CONTENT);

        // When
        final FileCredentials credential = lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasFileName(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveCustomisableFileName() {
        // Given
        final CreateSecretResult foo = createFileSecret(CONTENT, FILENAME);

        // When
        final FileCredentials credential = lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasFileName(FILENAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveContent() {
        // Given
        final CreateSecretResult foo = createFileSecret(CONTENT);

        // When
        final FileCredentials credential = lookup(FileCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasContent(CONTENT);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretResult foo = createFileSecret(CONTENT);
        final FileCredentials ours = lookup(FileCredentials.class, foo.getName());

        final FileCredentials theirs = new FileCredentialsImpl(null, "id", "description", "filename", SecretBytes.fromBytes(CONTENT));

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final CreateSecretResult foo = createFileSecret(CONTENT);

        // When
        final ListBoxModel list = jenkins.getCredentials().list(FileCredentials.class);

        // Then
        assertThat(list)
                .containsOption(foo.getName(), foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretResult foo = createFileSecret(CONTENT);

        // When
        final WorkflowRun run = runPipeline("",
                "node {",
                "  withCredentials([file(credentialsId: '" + foo.getName() + "', variable: 'FILE')]) {",
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
        final CreateSecretResult foo = createFileSecret(CONTENT);

        // When
        final WorkflowRun run = runPipeline("",
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
        final CreateSecretResult foo = createFileSecret(CONTENT);
        final FileCredentials before = lookup(FileCredentials.class, foo.getName());

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

    private CreateSecretResult createFileSecret(byte[] content) {
        final List<Tag> tags = Lists.of(AwsTags.type(Type.file));

        return createSecret(content,tags);
    }

    private CreateSecretResult createFileSecret(byte[] content, String filename) {
        final List<Tag> tags = Lists.of(AwsTags.type(Type.file), AwsTags.filename(filename));

        return createSecret(content, tags);
    }

    private CreateSecretResult createSecret(byte[] content, List<Tag> tags) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretBinary(ByteBuffer.wrap(content))
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return jenkins.getCredentials().lookup(type, id);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
