package io.jenkins.plugins.aws_secrets_manager_credentials_provider.aws;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Look up all secrets in Secrets Manager using the ListSecrets command.
 *
 * Paginate through secrets until there are none left to get.
 */
public class ListSecretsOperation implements Supplier<List<SecretListEntry>> {

    private final AWSSecretsManager client;

    public ListSecretsOperation(AWSSecretsManager client) {
        this.client = client;
    }

    @Override
    public List<SecretListEntry> get() {
        final List<SecretListEntry> secretList = new ArrayList<>();

        Optional<String> nextToken = Optional.empty();
        do {
            final ListSecretsRequest req = nextToken.map((nt) -> new ListSecretsRequest().withNextToken(nt)).orElse(new ListSecretsRequest());
            final ListSecretsResult res = client.listSecrets(req);
            final List<SecretListEntry> secrets = res.getSecretList();
            secretList.addAll(secrets);
            nextToken = Optional.ofNullable(res.getNextToken());
        } while (nextToken.isPresent());

        return secretList;
    }
}
