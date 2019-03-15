package io.jenkins.plugins.aws_secrets_manager_credentials_provider;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cloud.localstack.TestUtils;
import hudson.security.ACL;
import hudson.util.Secret;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.fixtures.AwsSecret;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.ConfiguredWithCode;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.JenkinsConfiguredWithCodeRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

/*
 * This integration test is complicated by the current lack of support for ListSecrets or
 * DeleteSecret in Moto.
 *
 * We CAN avoid the requirement for DeleteSecret by defining several fixture
 * secrets upfront, and creating all of them before test setup. All tests share the same secrets.
 * Each test asserts that the returned credentials list contains the secret it is interested in.
 *
 * We CANNOT avoid the requirement for ListSecrets because that is the mechanism that the plugin
 * uses to retrieve secrets from Secrets Manager. We work around this by cloning a forked copy
 * of Moto that has support for ListSecrets. This forked copy of Moto is then started up by the
 * Maven Exec Plugin (see the definition in pom.xml) in the pre-integration-test phase. If you are
 * not running this test case with Maven then you MUST start up Moto manually with an out-of-band
 * command BEFORE running the tests.
 *
 * In future, Moto will gain support for these features, and we will be able to write this test case
 * more cleanly.
 */
public class PluginIT {

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private CredentialsStore store;

    private List<BaseStandardCredentials> lookupCredentials() {
        return CredentialsProvider.lookupCredentials(BaseStandardCredentials.class, r.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    private static void createSecrets(AwsSecret... secrets) {
        for (AwsSecret s: secrets) {
            final List<Tag> t = s.getTags().entrySet().stream()
                    .map((entry) -> new Tag().withKey(entry.getKey()).withValue(entry.getValue()))
                    .collect(Collectors.toList());

            final CreateSecretRequest request = new CreateSecretRequest()
                    .withName(s.getName())
                    .withDescription(s.getDescription())
                    .withSecretString(s.getValue())
                    .withTags(t);

            final CreateSecretResult result = TestUtils.getClientSecretsManager().createSecret(request);

            if (result.getSdkHttpMetadata().getHttpStatusCode() >= 400) {
                throw new RuntimeException("Failed to prime the Secrets Manager mock.");
            }
        }
    }

    @BeforeClass
    public static void fakeAwsCredentials() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretKey", "test");
    }

    @Before
    public void setup() {
        store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();

        createSecrets(AwsSecret.TEXT, AwsSecret.PRIVATE_KEY, AwsSecret.TAG_COYOTE, AwsSecret.TAG_ROADRUNNER);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportStringSecrets() {
        // Given
        final AwsSecret secret = AwsSecret.TEXT;

        // When
        final List<BaseStandardCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "description", "secret")
                .contains(tuple(secret.getName(), secret.getDescription(), Secret.fromString(secret.getValue())));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportMultiLineSecrets() {
        // Given
        final AwsSecret secret = AwsSecret.PRIVATE_KEY;

        // When
        final List<BaseStandardCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "description", "secret")
                .contains(tuple(secret.getName(), secret.getDescription(), Secret.fromString(secret.getValue())));
    }

    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldFilterByTag() {
        // Given
        final AwsSecret secret = AwsSecret.TAG_ROADRUNNER;
        final AwsSecret excluded = AwsSecret.TAG_COYOTE;

        // When
        final List<BaseStandardCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "description", "secret")
                .contains(tuple(secret.getName(), secret.getDescription(), Secret.fromString(secret.getValue())))
                .doesNotContain(tuple(excluded.getName(), excluded.getDescription(), Secret.fromString(excluded.getValue())));
    }

    @Ignore("We cannot test this until Moto supports DeleteSecret")
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldStartEmpty() {
        // When
        final List<BaseStandardCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final StringCredentialsImpl credential = new StringCredentialsImpl(CredentialsScope.GLOBAL, "id", "desc", Secret.fromString("password"));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "id", "desc", Secret.fromString("password"))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "id", "desc", Secret.fromString("password"))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }

}
