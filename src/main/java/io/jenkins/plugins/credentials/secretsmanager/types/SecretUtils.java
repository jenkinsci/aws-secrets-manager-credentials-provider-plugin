package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// FIXME rename this class to something better
class SecretUtils {

    static String getSecretValue(AWSSecretsManager client, String secretName) throws IOException, InterruptedException {
        final GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);

        final GetSecretValueResult result;
        try {
            // TODO configure the timeout
            result = client.getSecretValue(request);
        } catch (AmazonServiceException ex) {
            throw new IOException(ex);
        } catch (AmazonClientException ex) {
            throw new InterruptedException(ex.getMessage());
        }

        // Which field is populated depends on whether the secret was a string or binary
        if (result.getSecretString() != null) {
            return result.getSecretString();
        } else {
            return StandardCharsets.UTF_8.decode(result.getSecretBinary()).toString();
        }
    }

    static CredentialProxy createProxy(AWSSecretsManager client) throws Exception {
        final ListSecretsRequest request = new ListSecretsRequest();
        final ListSecretsResult result = client.listSecrets(request);
        final List<String> reservedWords = Arrays.asList("password", "privateKey", "privateKeys", "keyStore");

        for (SecretListEntry e: result.getSecretList()) {
            final String id = e.getName();
            final String description = e.getDescription();
            // FIXME have to set the 'id' and 'description' on the map from the result too
            final Map<String, Object> properties = e.getTags().stream()
                    .filter(tag -> !reservedWords.contains(tag.getKey()))
                    .collect(Collectors.toMap(Tag::getKey, Tag::getValue));

            if (isCertificateCredential(properties)) {
                return CredentialProxy.certificate(client, properties);
            } else if (isSshCredential(properties)) {
                return CredentialProxy.sshKey(client, properties);
            } else if (isUsernamePasswordCredential(properties)) {
                return CredentialProxy.usernamePassword(client, properties);
            } else {
                return CredentialProxy.secretText(client, properties);
            }
        }
    }

    private static boolean isUsernamePasswordCredential(Map<String, Object> properties) {
        return properties.containsKey("username");
    }

    private static boolean isSshCredential(Map<String, Object> properties) {
        return false;
    }

    private static boolean isCertificateCredential(Map<String, Object> properties) {
        return false;
    }

}
