package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.services.secretsmanager.model.*;

/**
 * Guarantees to wipe any secrets left in Secrets Manager before your test.
 */
public class AutoErasingAWSSecretsManagerRule extends AWSSecretsManagerRule {

    @Override
    public void before() {
        super.before();

        clear();
    }

    private void clear() {
        final ListSecretsResult listSecretsResult = getClient().listSecrets(new ListSecretsRequest().withMaxResults(100));

        for(SecretListEntry entry: listSecretsResult.getSecretList()) {
            final String secretId = entry.getName();

            try {
                getClient().restoreSecret(new RestoreSecretRequest().withSecretId(secretId));
                getClient().deleteSecret(new DeleteSecretRequest().withSecretId(secretId).withForceDeleteWithoutRecovery(true));
            } catch (ResourceNotFoundException e) {
                // Don't care
            }
        }
    }
}
