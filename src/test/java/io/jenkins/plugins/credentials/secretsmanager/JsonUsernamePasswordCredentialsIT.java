package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Format;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.AwsTags;
import io.jenkins.plugins.credentials.secretsmanager.util.CredentialNames;
import io.jenkins.plugins.credentials.secretsmanager.util.CredentialSnapshots;
import io.jenkins.plugins.credentials.secretsmanager.util.MyJenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;

/**
 * The plugin should support Username With Password credentials whose payload is a JSON document
 * (tagged with {@code jenkins:credentials:format=json}), keeping the username out of AWS tags.
 */
public class JsonUsernamePasswordCredentialsIT {

    private static final String USERNAME = "joe";
    private static final String PASSWORD = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsernameAndPasswordFromJson() {
        final var secret = createJsonSecret(plainJson(USERNAME, PASSWORD));

        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.name());

        assertThat(credential)
                .hasUsername(USERNAME)
                .hasPassword(PASSWORD)
                .hasId(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldDecodeBase64Fields() {
        final var secret = createJsonSecret(base64Json(USERNAME, PASSWORD));

        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.name());

        assertThat(credential)
                .hasUsername(USERNAME)
                .hasPassword(PASSWORD);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        final var secret = createJsonSecret(plainJson(USERNAME, PASSWORD));

        final var run = runPipeline("",
                "withCredentials([usernamePassword(credentialsId: '" + secret.name() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}");

        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        final var foo = createJsonSecret(plainJson(USERNAME, PASSWORD));
        final StandardUsernamePasswordCredentials before =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, foo.name());

        final StandardUsernamePasswordCredentials after = CredentialSnapshots.snapshot(before);

        assertThat(after)
                .hasUsername(before.getUsername())
                .hasPassword(before.getPassword())
                .hasId(before.getId());
    }

    private CreateSecretResponse createJsonSecret(String payload) {
        final var tags = List.of(
                AwsTags.type(Type.usernamePassword),
                AwsTags.tag(Tags.format, Format.json));

        return secretsManager.getClient().createSecret(b -> {
            b.name(CredentialNames.random());
            b.secretString(payload);
            b.tags(tags);
        });
    }

    private static String plainJson(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private static String base64Json(String username, String password) {
        final String u = Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8));
        final String p = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        return "{"
                + "\"username\":\"" + u + "\",\"username_encoding\":\"base64\","
                + "\"password\":\"" + p + "\",\"password_encoding\":\"base64\""
                + "}";
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
