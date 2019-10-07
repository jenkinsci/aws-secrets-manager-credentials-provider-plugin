package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;

/**
 * A multi-type credential class backed by AWS Secrets Manager, which detects its type at lookup
 * time.
 * <p>
 * NOTE: The underlying AWS secret must have the necessary format and metadata to be used as a
 * particular credential type. If these things are not present, the relevant accessor method(s) will
 * fail at lookup time. (For example, to use the AWS secret as a Jenkins {@link SSHUserPrivateKey},
 * the secretString must be in private key format, and username metadata must be present in the
 * secret's tags.)
 */
@NameWith(value = AwsCredentials.NameProvider.class)
class AwsCredentials extends BaseStandardCredentials implements StringCredentials, StandardUsernamePasswordCredentials, SSHUserPrivateKey, StandardCertificateCredentials {

    private static final char[] EMPTY_PASSWORD = {};
    private static final Secret NONE = Secret.fromString("");
    private static final long serialVersionUID = 1L;
    static final String USERNAME_TAG = "jenkins:credentials:username";

    public Map<String, String> getTags() {
        return tags;
    }

    private final Map<String, String> tags;

    private transient final AWSSecretsManager client = PluginConfiguration.getInstance().getClient();

    AwsCredentials(String id, String description, Map<String, String> tags) {
        super(id, description);
        this.tags = tags;
    }

    @Nonnull
    @Override
    public Secret getSecret() {
        return Secret.fromString(getSecretString(getId()));
    }

    @NonNull
    @Override
    public Secret getPassword() {
        if (tags.containsKey(USERNAME_TAG)) {
            // username/password
            return Secret.fromString(getSecretString(getId()));
        } else {
            // certificate
            return NONE;
        }
    }

    @NonNull
    @Override
    public String getUsername() {
        if (tags.containsKey(USERNAME_TAG)) {
            return tags.get(USERNAME_TAG);
        } else {
            throw new CredentialsUnavailableException("username", Messages.noUsernameError());
        }
    }

    @Override
    public Secret getPassphrase() {
        return NONE;
    }

    @NonNull
    @Override
    public List<String> getPrivateKeys() {
        return Collections.singletonList(this.getPrivateKey());
    }

    @NonNull
    @Deprecated
    @Override
    public String getPrivateKey() {
        final String secretValue = getSecretString(getId());

        if (isValidSSHKey(secretValue)) {
            return secretValue;
        } else {
            throw new CredentialsUnavailableException("privateKey", Messages.noPrivateKeyError());
        }
    }

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        final ByteBuffer secretValue = getSecretBinary(getId());

        try (InputStream stream = new ByteArrayInputStream(secretValue.array())) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // JDK9 workaround: PKCS#12 keystores must have at least an empty password (not null)
            keyStore.load(stream, EMPTY_PASSWORD);
            return keyStore;
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new CredentialsUnavailableException("keyStore", Messages.noCertificateError());
        }
    }

    private String getSecretString(String secretName) {
        final GetSecretValueResult result = this.getSecretValue(secretName);
        return result.getSecretString();
    }

    private ByteBuffer getSecretBinary(String secretName) {
        final GetSecretValueResult result = this.getSecretValue(secretName);
        return result.getSecretBinary();
    }

    GetSecretValueResult getSecretValue() {
        return getSecretValue(this.getId());
    }

    private GetSecretValueResult getSecretValue(String secretName) {
        final GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);

        try {
            return client.getSecretValue(request);
        } catch (AmazonClientException ex) {
            throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(secretName));
        }
    }

    private static boolean isValidSSHKey(String privateKey) {
        return SSHKeyValidator.isValid(privateKey);
    }

    public static class NameProvider extends CredentialsNameProvider<AwsCredentials> {
        @NonNull
        @Override
        public String getName(@NonNull AwsCredentials c) {
            return c.getId();
        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.awsSecretsManagerSecret();
        }
    }
}
