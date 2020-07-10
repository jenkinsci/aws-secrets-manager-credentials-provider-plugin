package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FiltersIT {

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins)
            .around(secretsManager);

    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldFilterByTag() {
        // Given
        final CreateSecretResult foo = createSecret(CredentialNames.random(), "supersecret", Lists.of(AwsTags.type(Type.string), AwsTags.tag("product", "roadrunner")));
        final CreateSecretResult bar = createSecret(CredentialNames.random(), "supersecret", Lists.of(AwsTags.type(Type.string), AwsTags.tag("product", "coyote")));

        // When
        final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }
    
    @Test
    @ConfiguredWithCode(value = "/name.yml")
    public void shouldFilterByName() {
        filterByName();
    }

    @Test
    @ConfiguredWithCode(value = "/name-and-tag.yml")
    public void shouldFilterByNameAndTag() {
        filterByName();
    }

    private void filterByName() {
        // Given
        final CreateSecretResult foo = createSecret("dev/jenkins/secret", "supersecret", Lists.of(AwsTags.type(Type.string), AwsTags.tag("product", "roadrunner")));
        final CreateSecretResult bar = createSecret("itg/jenkins/secret", "supersecret", Lists.of(AwsTags.type(Type.string), AwsTags.tag("product", "coyote")));

        // When
        final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    private CreateSecretResult createSecret(String secretName, String secretString, List<Tag> tags) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(secretName)
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }
}
