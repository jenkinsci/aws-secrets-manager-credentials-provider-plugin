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

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
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
            final PEMKeyPair object = (PEMKeyPair) pemParser.readObject();
            final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            final KeyPair keyPair = converter.getKeyPair(object);
            final StringWriter writer = new StringWriter();
            final JcaPEMWriter w = new JcaPEMWriter(writer);
            w.writeObject(keyPair);
            w.flush();
            return writer.toString();
        } catch (IOException e) {
            throw new CredentialsUnavailableException("privateKey", Messages.noPrivateKeyError());
        }
    }

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        try {
            //final PEMParser pemParser = new PEMParser(new StringReader(getSecretValue(getId())));
            //final Object object = pemParser.readObject();
            //final JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider("BC");
            //final Certificate certificate = converter.getCertificate((X509CertificateHolder) object);
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final String secretValue = getSecretValue(getId());
            final InputStream stream = IOUtils.toInputStream(secretValue, StandardCharsets.UTF_8);
            keyStore.load(stream, null);
            return keyStore;
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
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
