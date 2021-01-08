package io.jenkins.plugins.credentials.secretsmanager.config.fields.name.removePrefix;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public abstract class CheckTransformationIT {

    public final JenkinsConfiguredWithWebRule jenkins = new JenkinsConfiguredWithWebRule();
    public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins)
            .around(secretsManager);

    protected abstract FormValidationResult validate(String prefix);

    @Before
    public void setEndpointConfiguration() {
        jenkins.configure(form -> {
            new PluginConfigurationForm(form).setEndpointConfiguration("http://localhost:4584", "us-east-1");
        });
    }

    @Test
    public void shouldAllowGoodTransformation() {
        // Given
        createSecretWithName("staging-foo");
        createSecretWithName("production-foo");

        // When
        final FormValidationResult result = validate("staging-");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.isSuccess()).as("Success").isTrue();
            s.assertThat(result.getMessage()).as("Message").startsWith(Messages.success());
        });
    }

    @Test
    public void shouldRejectBadTransformation() {
        // Given
        createSecretWithName("staging-foo");
        createSecretWithName("foo");

        // When
        final FormValidationResult result = validate("staging-");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.isSuccess()).as("Success").isFalse();
            s.assertThat(result.getMessage()).as("Message").startsWith(Messages.transformationProducedCredentialIdsThatWereNotUnique());
        });
    }

    private void createSecretWithName(String name) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(name)
                .withSecretString("supersecret")
                .withTags(AwsTags.type(Type.string));

        secretsManager.getClient().createSecret(request);
    }
}
