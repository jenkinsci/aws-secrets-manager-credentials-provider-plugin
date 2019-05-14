package io.jenkins.plugins.aws_secrets_manager_credentials_provider.aws;

import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ListSecretsOperationTest {

    @Test
    public void shouldHandleMissingSecret() {
        final ListSecretsResult result = new ListSecretsResult().withSecretList();
        final ListSecretsOperation strategy = new ListSecretsOperation(new ListingMockAwsSecretsManager(result));

        final List<SecretListEntry> secrets = strategy.get();

        assertThat(secrets).isEmpty();
    }

    @Test
    public void shouldHandleSecret() {
        final ListSecretsResult result = new ListSecretsResult().withSecretList(new SecretListEntry().withName("foo").withDescription("bar"));
        final ListSecretsOperation strategy = new ListSecretsOperation(new ListingMockAwsSecretsManager(result));

        final List<SecretListEntry> secrets = strategy.get();

        assertThat(secrets).containsExactly(new SecretListEntry().withDescription("bar").withName("foo"));
    }

    @Test
    public void shouldHandleSecretWithTags() {
        final ListSecretsResult result = new ListSecretsResult().withSecretList(new SecretListEntry().withName("foo").withDescription("bar").withTags(new Tag().withKey("key").withValue("value")));
        final ListSecretsOperation strategy = new ListSecretsOperation(new ListingMockAwsSecretsManager(result));

        final List<SecretListEntry> secrets = strategy.get();

        assertThat(secrets).containsExactly(new SecretListEntry().withDescription("bar").withName("foo").withTags(new Tag().withKey("key").withValue("value")));
    }

    private static class ListingMockAwsSecretsManager extends MockAwsSecretsManager {

        private final ListSecretsResult result;

        private ListingMockAwsSecretsManager(ListSecretsResult result) {
            this.result = result;
        }

        @Override
        public ListSecretsResult listSecrets(ListSecretsRequest listSecretsRequest) {
            return result;
        }
    }
}
