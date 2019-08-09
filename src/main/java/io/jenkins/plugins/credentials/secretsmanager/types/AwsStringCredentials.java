package io.jenkins.plugins.credentials.secretsmanager.types;

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
import io.jenkins.plugins.credentials.secretsmanager.Messages;

public class AwsStringCredentials extends BaseStandardCredentials implements StringCredentials, StandardUsernamePasswordCredentials, SSHUserPrivateKey, StandardCertificateCredentials {

    private static final Secret NO_PASSPHRASE = Secret.fromString("");
    private static final long serialVersionUID = 1L;

    private final Map<String, String> tags;

    private final transient AWSSecretsManager client;

    AwsStringCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description);
        this.tags = tags;
        this.client = client;
    }

    /**
     * from StringCredentials
     */
    @Nonnull
    @Override
    public Secret getSecret() {
        final String id = this.getId();
        return Secret.fromString(getSecretValue(id));
    }

    /**
     * from StandardUsernamePasswordCredentials
     */
    @NonNull
    @Override
    public Secret getPassword() {
        final String id = this.getId();
        return Secret.fromString(getSecretValue(id));
    }

    @NonNull
    @Override
    public String getUsername() {
        return tags.get("username");
    }

    /**
     * from SSHUserPrivateKey
     */
    @NonNull
    @Override
    public String getPrivateKey() {
        final String id = this.getId();

        final PEMParser pemParser = new PEMParser(new StringReader(getSecretValue(id)));

        try {
            final Object object = pemParser.readObject();
            final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            final KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);
            return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(keyPair.getPrivate().getEncoded())).toString();
        } catch (IOException e) {
            throw new CredentialsUnavailableException("secret", "Private key secret value was not encoded in PEM format");
        }
    }

    /**
     * from SSHUserPrivateKey
     */
    @Override
    public Secret getPassphrase() {
        return NO_PASSPHRASE;
    }

    /**
     * from SSHUserPrivateKey
     */
    @NonNull
    @Override
    public List<String> getPrivateKeys() {
        return Collections.singletonList(this.getPrivateKey());
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

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        return null;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        public DescriptorImpl() {
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.secretText();
        }
    }
}
