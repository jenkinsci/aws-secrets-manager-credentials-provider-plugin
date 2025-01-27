package io.jenkins.plugins.credentials.secretsmanager.factory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.factory.certificate.AwsCertificateCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.file.AwsFileCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.git_app.GitCredentialFactory;
import io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key.AwsSshUserPrivateKey;
import io.jenkins.plugins.credentials.secretsmanager.factory.string.AwsStringCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.username_password.AwsUsernamePasswordCredentials;

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
     * @param client the Secrets Manager client that will retrieve the secret's value on demand
     * @return a credential (if one could be constructed from the secret's properties)
     */
    public static Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final String type = tags.getOrDefault(Tags.type, "");
        final String username = tags.getOrDefault(Tags.username, "");
        final String filename = tags.getOrDefault(Tags.filename, name);
        final String appId = tags.getOrDefault(Tags.appid, "");

        switch (type) {
            case Type.string:
                return Optional.of(new AwsStringCredentials(name, description, new SecretSupplier(client, arn)));
            case Type.usernamePassword:
                return Optional.of(new AwsUsernamePasswordCredentials(name, description, new SecretSupplier(client, arn), username));
            case Type.sshUserPrivateKey:
                return Optional.of(new AwsSshUserPrivateKey(name, description, new StringSupplier(client, arn), username));
            case Type.certificate:
                return Optional.of(new AwsCertificateCredentials(name, description, new SecretBytesSupplier(client, arn)));
            case Type.file:
                return Optional.of(new AwsFileCredentials(name, description, filename, new SecretBytesSupplier(client, arn)));
            case Type.githubApp:
                return GitCredentialFactory.createCredential(name, description, appId, new StringSupplier(client, name));
            default:
                return Optional.empty();
        }
    }

    private static class SecretBytesSupplier extends RealSecretsManager implements Supplier<SecretBytes> {

        private SecretBytesSupplier(AWSSecretsManager client, String name) {
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

        private SecretSupplier(AWSSecretsManager client, String name) {
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

        private StringSupplier(AWSSecretsManager client, String name) {
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
        private final transient AWSSecretsManager client;

        RealSecretsManager(AWSSecretsManager client, String id) {
            this.client = client;
            this.id = id;
        }

        @NonNull
        SecretValue getSecretValue() {
            try {
                final GetSecretValueResult result = client.getSecretValue(new GetSecretValueRequest().withSecretId(id));
                if (result.getSecretBinary() != null) {
                    return SecretValue.binary(result.getSecretBinary().array());
                }
                if (result.getSecretString() != null) {
                    return SecretValue.string(result.getSecretString());
                }
                throw new IllegalStateException(Messages.emptySecretError(id));
            } catch (AmazonClientException ex) {
                LOG.warning("AWS Secrets Manager retrieval error");
                LOG.warning(ex.getMessage());

                throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(id));
            }
        }
    }
}
