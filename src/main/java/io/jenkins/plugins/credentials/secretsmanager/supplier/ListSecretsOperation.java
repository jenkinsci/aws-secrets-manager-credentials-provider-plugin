package io.jenkins.plugins.credentials.secretsmanager.supplier;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.Filter;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Look up all secrets in Secrets Manager using the ListSecrets command. Paginate through secrets
 * until there are none left to get.
 */
class ListSecretsOperation implements Supplier<Collection<SecretListEntry>> {

    private final SecretsManagerClient client;

    private final Collection<Filter> filters;

    ListSecretsOperation(SecretsManagerClient client, Collection<Filter> filters) {
        this.client = client;
        this.filters = filters;
    }

    @Override
    public Collection<SecretListEntry> get() {
        final List<SecretListEntry> secretList = new ArrayList<>();

        Optional<String> nextToken = Optional.empty();
        do {
            final var request = ListSecretsRequest.builder()
                    .filters(filters);

            nextToken.ifPresent((nt) -> {
                request.nextToken(nt);
            });

            final var result = client.listSecrets(request.build());

            final List<SecretListEntry> secrets = result.secretList()
                    .stream()
                    .filter(ListSecretsOperation::isNotDeleted)
                    .toList();

            secretList.addAll(secrets);

            nextToken = Optional.ofNullable(result.nextToken());
        } while (nextToken.isPresent());

        return secretList;
    }

    private static boolean isNotDeleted(SecretListEntry entry) {
        return entry.deletedDate() == null;
    }
}
