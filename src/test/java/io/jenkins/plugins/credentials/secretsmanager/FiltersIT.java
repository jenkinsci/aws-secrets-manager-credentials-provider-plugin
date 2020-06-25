package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FiltersIT {

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins)
            .around(secretsManager);

    @Test
    @ConfiguredWithCode(value = "/filters.yml")
    public void shouldFilterCredentials() {
        // Given
        final CreateSecretResult foo = createSecretWithTag("product", "foo");
        final CreateSecretResult bar = createSecretWithTag("product", "bar");

        // When
        final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id")
                .contains(foo.getName())
                .doesNotContain(bar.getName());
    }

    private CreateSecretResult createSecretWithTag(String key, String value) {
        return createSecret("supersecret", Lists.of(AwsTags.type(Type.string), AwsTags.tag(key, value)));
    }

    private CreateSecretResult createSecret(String secretString, List<Tag> tags) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }
}
