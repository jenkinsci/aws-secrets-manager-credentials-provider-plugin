package io.jenkins.plugins.credentials.secretsmanager.supplier;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse;
import org.junit.Test;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ListSecretsOperationTest {

    @Test
    public void shouldHandleMissingSecret() {
        final var response = ListSecretsResponse.builder().build();
        final var strategy = new ListSecretsOperation(new MockAwsSecretsManager(response), Collections.emptyList());

        final var secrets = strategy.get();

        assertThat(secrets).isEmpty();
    }

    @Test
    public void shouldHandleSecret() {
        final var response = ListSecretsResponse.builder()
                .secretList(secret -> {
                    secret.name("foo");
                    secret.description("bar");
                })
                .build();

        final var strategy = new ListSecretsOperation(new MockAwsSecretsManager(response), Collections.emptyList());

        final var secrets = strategy.get();

        assertThat(secrets).containsExactly(SecretListEntry.builder().description("bar").name("foo").build());
    }

    @Test
    public void shouldHandleSecretWithTags() {
        final var response = ListSecretsResponse.builder()
                .secretList(secret -> {
                    secret.name("foo");
                    secret.description("bar");
                    secret.tags(tag -> tag.key("key").value("value"));
                })
                .build();

        final var strategy = new ListSecretsOperation(new MockAwsSecretsManager(response), Collections.emptyList());

        final var secrets = strategy.get();

        assertThat(secrets).containsExactly(
                SecretListEntry.builder().description("bar").name("foo").tags(tag -> tag.key("key").value("value")).build());
    }

    private static class MockAwsSecretsManager implements SecretsManagerClient {

        private final ListSecretsResponse response;

        private MockAwsSecretsManager(ListSecretsResponse response) {
            this.response = response;
        }

        @Override
        public ListSecretsResponse listSecrets(ListSecretsRequest listSecretsRequest) {
            return response;
        }

        @Override
        public String serviceName() {
            return "";
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
