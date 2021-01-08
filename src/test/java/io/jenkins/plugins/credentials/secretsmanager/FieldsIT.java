package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(Enclosed.class)
public class FieldsIT {

    /**
     * Test transformations of the credential's ID field.
     */
    public static class Id {

        private static final String SECRET_STRING = "supersecret";

        public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
        public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();

        @Rule
        public final RuleChain chain = RuleChain
                .outerRule(Rules.awsAccessKey("fake", "fake"))
                .around(jenkins)
                .around(secretsManager);

        @Test
        @ConfiguredWithCode(value = "/rename.yml")
        public void shouldTransform() {
            // Given
            final CreateSecretResult foo = createSecretWithName("staging-foo", SECRET_STRING);
            final CreateSecretResult bar = createSecretWithName("staging-bar", SECRET_STRING);
            final CreateSecretResult baz = createSecretWithName("baz", SECRET_STRING);

            // When
            final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

            // Then
            assertThat(credentials)
                    .extracting("id")
                    .contains("foo", "bar", "baz");
        }

        @Test
        @ConfiguredWithCode(value = "/rename.yml")
        public void shouldNotTransformWhenIdsClash() {
            // Given
            final CreateSecretResult stagingFoo = createSecretWithName("staging-foo", SECRET_STRING);
            final CreateSecretResult foo = createSecretWithName("foo", SECRET_STRING);

            // Then
            assertThatIllegalStateException()
                    .isThrownBy(() -> jenkins.getCredentials().lookup(StringCredentials.class))
                    .withMessageContaining("Duplicate key");
        }

        @Test
        @ConfiguredWithCode(value = "/rename.yml")
        public void shouldTransformAndUseOriginalIdToFetchSecret() {
            // Given
            final CreateSecretResult stagingFoo = createSecretWithName("staging-foo", SECRET_STRING);

            // When
            final StringCredentials foo = jenkins.getCredentials().lookup(StringCredentials.class, "foo");

            // Then
            assertThat(foo.getSecret().getPlainText())
                    .isEqualTo(SECRET_STRING);
        }

        private CreateSecretResult createSecretWithName(String name, String secretString) {
            final CreateSecretRequest request = new CreateSecretRequest()
                    .withName(name)
                    .withSecretString(secretString)
                    .withTags(AwsTags.type(Type.string));

            return secretsManager.getClient().createSecret(request);
        }
    }

    /**
     * Test transformations of the credential's description field.
     */
    public static class Description {

        private static final String DESCRIPTION = "foo bar";

        public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
        public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();

        @Rule
        public final RuleChain chain = RuleChain
                .outerRule(Rules.awsAccessKey("fake", "fake"))
                .around(jenkins)
                .around(secretsManager);

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldShowDescriptionByDefault() {
            //Given
            final CreateSecretResult secret = createSecretWithDescription(DESCRIPTION);

            // When
            final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

            // Then
            assertThat(credentials)
                    .extracting("id", "description")
                    .contains(tuple(secret.getName(), DESCRIPTION));
        }

        @Test
        @ConfiguredWithCode(value = "/no-description.yml")
        public void shouldHideDescription() {
            // Given
            final CreateSecretResult secret = createSecretWithDescription(DESCRIPTION);

            // When
            final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

            // Then
            assertThat(credentials)
                    .extracting("id", "description")
                    .contains(tuple(secret.getName(), ""));
        }

        private CreateSecretResult createSecretWithDescription(String description) {
            final CreateSecretRequest request = new CreateSecretRequest()
                    .withName(CredentialNames.random())
                    .withSecretString("supersecret")
                    .withDescription(description)
                    .withTags(AwsTags.type(Type.string));

            return secretsManager.getClient().createSecret(request);
        }

    }
}
