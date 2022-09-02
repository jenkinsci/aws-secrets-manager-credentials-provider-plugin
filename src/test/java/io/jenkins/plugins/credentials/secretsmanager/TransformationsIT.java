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
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(Enclosed.class)
public class TransformationsIT {

    /**
     * Test transformations of the secret name.
     */
    public static class Name {

        private static final String SECRET_STRING = "supersecret";

        public MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
        public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

        @Rule
        public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

        @Test
        @ConfiguredWithCode(value = "/transformations/removePrefix.yml")
        public void shouldRemovePrefix() {
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
        @ConfiguredWithCode(value = "/transformations/removePrefixes.yml")
        public void shouldRemovePrefixes() {
            // Given
            final CreateSecretResult foo = createSecretWithName("staging-foo", SECRET_STRING);
            final CreateSecretResult bar = createSecretWithName("production-bar", SECRET_STRING);
            final CreateSecretResult baz = createSecretWithName("baz", SECRET_STRING);

            // When
            final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

            // Then
            assertThat(credentials)
                    .extracting("id")
                    .contains("foo", "bar", "baz");
        }

        @Test
        @ConfiguredWithCode(value = "/transformations/removePrefix.yml")
        public void shouldStillResolveAfterTransformation() {
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
     * Test transformations of the secret description.
     */
    public static class Description {

        private static final String DESCRIPTION = "foo bar";

        public MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
        public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

        @Rule
        public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

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
        @ConfiguredWithCode(value = "/transformations/no-description.yml")
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
