package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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

/**
 * A multi-type credential class backed by AWS Secrets Manager, which detects its type at lookup
 * time.
 * <p>
 * Due to its dynamic behavior, an instance of this class can either be bound in your Jenkins job as
 * its true type (e.g. SSH private key), or upcast to a simple Secret Text credential.
 * <p>
 * NOTE: a credential cannot necessarily be downcast to a complex type: if the additional metadata
 * is missing on the underlying AWS secret, the relevant accessor methods will fail at lookup time.
 */
public class AwsCredentials extends BaseStandardCredentials implements StringCredentials, StandardUsernamePasswordCredentials, SSHUserPrivateKey, StandardCertificateCredentials {

    private static final char[] EMPTY_PASSWORD = new char[]{};
    private static final Secret NONE = Secret.fromString("");
    private static final long serialVersionUID = 1L;

    private final Map<String, String> tags;

    private final transient AWSSecretsManager client;

    AwsCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description);
        this.tags = tags;
        this.client = client;
    }

    @Nonnull
    @Override
    public Secret getSecret() {
        return Secret.fromString(getSecretString(getId()));
    }

    @NonNull
    @Override
    public Secret getPassword() {
        if (tags.containsKey("username")) {
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
        if (tags.containsKey("username")) {
            return tags.get("username");
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
        try {
            final String secretValue = getSecretString(getId());
            final PEMParser pemParser = new PEMParser(new StringReader(secretValue));
            final Object keyObject = pemParser.readObject();

            if ((keyObject instanceof PEMKeyPair) || (keyObject instanceof PrivateKeyInfo)) {
                return secretValue.trim();
            } else {
                throw new CredentialsUnavailableException("privateKey", Messages.noPrivateKeyError());
            }
        } catch (IOException e) {
            throw new CredentialsUnavailableException("privateKey", Messages.noPrivateKeyError());
        }
    }

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        final ByteBuffer secretValue = getSecretBinary(getId());

        try (InputStream stream = new ByteArrayInputStream(secretValue.array())) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // JDK9 workaround: PKCS#12 keystores must always have a password (not null)
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

    private GetSecretValueResult getSecretValue(String secretName) {
        final GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);

        try {
            // TODO configure the timeout
            return client.getSecretValue(request);
        } catch (AmazonClientException ex) {
            throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveSecretError(secretName));
        }
    }


    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        public DescriptorImpl() {
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.awsSecret();
        }
    }
}
