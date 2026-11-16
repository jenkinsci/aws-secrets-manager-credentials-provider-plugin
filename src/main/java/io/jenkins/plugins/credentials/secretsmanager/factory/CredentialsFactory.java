package io.jenkins.plugins.credentials.secretsmanager.factory;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.FolderScope;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.ScopedCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.certificate.AwsCertificateCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.file.AwsFileCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.git_app.GitHubAppCredentialsFactory;
import io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key.AwsSshUserPrivateKey;
import io.jenkins.plugins.credentials.secretsmanager.factory.string.AwsStringCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.username_password.AwsUsernamePasswordCredentials;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class CredentialsFactory {

    private CredentialsFactory() {

    }

    /**
     * Construct a Jenkins credential from a Secrets Manager secret.
     *
     * @param name the secret's name (must be unique within the AWS account)
     * @param description the secret's description
     * @param tags the secret's AWS tags
     * @param client supplies the Secrets Manager client that will retrieve the secret's value on demand. Resolved at retrieval time, so a credential keeps working if the client is replaced.
     * @return a credential (if one could be constructed from the secret's properties)
     */
    public static Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, Supplier<SecretsManagerClient> client) {
        final String type = tags.getOrDefault(Tags.type, "");
        final String username = tags.getOrDefault(Tags.username, "");
        final String filename = tags.getOrDefault(Tags.filename, name);
        final String appId = tags.getOrDefault(Tags.appid, "");
        final String apiUri = tags.get(Tags.apiuri); // null if not present

        // Create the base credential based on type
        Optional<StandardCredentials> baseCredential;
        switch (type) {
            case Type.string:
                baseCredential = Optional.of(new AwsStringCredentials(name, description, new SecretSupplier(client, arn)));
                break;
            case Type.usernamePassword:
                baseCredential = Optional.of(new AwsUsernamePasswordCredentials(name, description, new SecretSupplier(client, arn), username));
                break;
            case Type.sshUserPrivateKey:
                baseCredential = Optional.of(new AwsSshUserPrivateKey(name, description, new StringSupplier(client, arn), username));
                break;
            case Type.certificate:
                baseCredential = Optional.of(new AwsCertificateCredentials(name, description, new SecretBytesSupplier(client, arn)));
                break;
            case Type.file:
                baseCredential = Optional.of(new AwsFileCredentials(name, description, filename, new SecretBytesSupplier(client, arn)));
                break;
            case Type.githubApp:
                baseCredential = GitHubAppCredentialsFactory.create(name, description, appId, apiUri, new SecretSupplier(client, arn));
                break;
            default:
                return Optional.empty();
        }

        // Wrap with folder scope information
        return baseCredential.map(cred -> {
            return new ScopedCredentials(cred, folderScope(tags));
        });
    }

    /**
     * Builds the folder scope of a secret from its tags.
     *
     * <p>The scope is the value of the {@code jenkins:credentials:folder} tag plus the value of every additional tag
     * whose key is that key followed by {@code :} and a suffix, e.g. {@code jenkins:credentials:folder:2} or
     * {@code jenkins:credentials:folder:backend}. The suffix is only there to make the keys unique. Several tags hold
     * several folders without a delimiter inside a value, because AWS does not allow a comma in a tag value.
     *
     * @param tags the secret's AWS tags
     * @return the folder scope, or the global scope if the secret has no folder tag
     */
    static FolderScope folderScope(Map<String, String> tags) {
        final List<String> values = new ArrayList<>();
        final String base = tags.get(Tags.folder);
        if (base != null) {
            values.add(base);
        }
        tags.entrySet().stream()
                .filter(e -> e.getKey().startsWith(Tags.folder + ":"))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .forEach(values::add);

        return values.isEmpty() ? FolderScope.global() : FolderScope.parse(String.join(",", values));
    }

    private static class SecretBytesSupplier extends RealSecretsManager implements Supplier<SecretBytes> {

        private SecretBytesSupplier(Supplier<SecretsManagerClient> client, String name) {
            super(client, name);
        }

        @Override
        public SecretBytes get() {
            return getSecretValue().match(new SecretValue.Matcher<SecretBytes>() {
                @Override
                public SecretBytes string(String str) {
                    return null;
                }

                @Override
                public SecretBytes binary(byte[] bytes) {
                    return SecretBytes.fromBytes(bytes);
                }
            });
        }
    }

    private static class SecretSupplier extends RealSecretsManager implements Supplier<Secret> {

        private SecretSupplier(Supplier<SecretsManagerClient> client, String name) {
            super(client, name);
        }

        @Override
        public Secret get() {
            return getSecretValue().match(new SecretValue.Matcher<Secret>() {
                @Override
                public Secret string(String str) {
                    return Secret.fromString(str);
                }

                @Override
                public Secret binary(byte[] bytes) {
                    return null;
                }
            });
        }
    }

    private static class StringSupplier extends RealSecretsManager implements Supplier<String> {

        private StringSupplier(Supplier<SecretsManagerClient> client, String name) {
            super(client, name);
        }

        @Override
        public String get() {
            return getSecretValue().match(new SecretValue.Matcher<String>() {
                @Override
                public String string(String str) {
                    return str;
                }

                @Override
                public String binary(byte[] bytes) {
                    return null;
                }
            });
        }
    }

    private static class RealSecretsManager {

        private static final Logger LOG = Logger.getLogger(RealSecretsManager.class.getName());

        private final String id;
        private final transient Supplier<SecretsManagerClient> client;

        RealSecretsManager(Supplier<SecretsManagerClient> client, String id) {
            this.client = client;
            this.id = id;
        }

        @NonNull
        SecretValue getSecretValue() {
            final var request = GetSecretValueRequest.builder().secretId(id).build();

            try {
                final var result = client.get().getSecretValue(request);
                if (result.secretBinary() != null) {
                    return SecretValue.binary(result.secretBinary().asByteArray());
                }
                if (result.secretString() != null) {
                    return SecretValue.string(result.secretString());
                }
                throw new IllegalStateException(Messages.emptySecretError(id));
            } catch (SecretsManagerException ex) {
                LOG.warning("AWS Secrets Manager retrieval error");
                LOG.warning(ex.getMessage());

                throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(id));
            }
        }
    }
}
