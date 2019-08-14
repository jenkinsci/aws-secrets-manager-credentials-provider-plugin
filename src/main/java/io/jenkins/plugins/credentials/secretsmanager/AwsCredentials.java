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

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
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

    private static final Secret NONE = Secret.fromString("");
    private static final long serialVersionUID = 1L;

    private final Map<String, String> tags;

    private final transient AWSSecretsManager client;

    AwsCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description);
        this.tags = tags;
        this.client = client;
    }

    private void testForTheType(String n) {
        if (tags.containsKey("username")) {
            // either ssh key or username/password
            String secret = getSecretValue(n);

            if (isSshKey(secret)) {
                // ssh key
            } else {
                // username/password
            }
        } else {
            // either secret text or certificate
            String secret = getSecretValue(n);

            if (isCertificate(secret)) {
                // certificate
            } else {
                // secret text
            }
        }
    }

    private static boolean isSshKey(String secret) {
        return secret.startsWith("--BEGIN SSH PRIVATE KEY--");
    }

    private static boolean isCertificate(String secret) {
        return secret.startsWith("--BEGIN CERTIFICATE--");
    }

    @Nonnull
    @Override
    public Secret getSecret() {
        return Secret.fromString(getSecretValue(getId()));
    }

    @NonNull
    @Override
    public Secret getPassword() {
        if (tags.containsKey("username")) {
            // username/password
            return Secret.fromString(getSecretValue(getId()));
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
            final PEMParser pemParser = new PEMParser(new StringReader(getSecretValue(getId())));
            final Object object = pemParser.readObject();
            final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            final KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);
            return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(keyPair.getPrivate().getEncoded())).toString();
        } catch (IOException e) {
            throw new CredentialsUnavailableException("privateKey", Messages.noPrivateKeyError());
        }
    }

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        try {
            final PEMParser pemParser = new PEMParser(new StringReader(getSecretValue(getId())));
            final Object object = pemParser.readObject();
        } catch (IOException e) {
            throw new CredentialsUnavailableException("keyStore", Messages.noCertificateError());

        }
    }

    private String getSecretValue(String secretName) {
        final GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);

        final GetSecretValueResult result;
        try {
            // TODO configure the timeout
            result = client.getSecretValue(request);
        } catch (AmazonClientException ex) {
            throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveSecretError(secretName));
        }

        // Which field is populated depends on whether the secret was a string or binary
        final String s;
        if (result.getSecretString() != null) {
            s = result.getSecretString();
        } else {
            s = StandardCharsets.UTF_8.decode(result.getSecretBinary()).toString();
        }

        return s;
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
