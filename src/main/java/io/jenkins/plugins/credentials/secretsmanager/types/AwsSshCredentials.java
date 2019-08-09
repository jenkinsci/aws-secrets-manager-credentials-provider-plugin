package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

class AwsSshCredentials extends BaseStandardCredentials implements SSHUserPrivateKey {

    private static final long serialVersionUID = 1L;
    private static final Secret NO_PASSPHRASE = null;
    private static final char[] NO_PASSWORD = {};

    private final String username;
    private final transient AWSSecretsManager client;

    AwsSshCredentials(String id, String description, String username, AWSSecretsManager client) {
        super(id, description);
        this.username = username;
        this.client = client;
    }

    @NonNull
    @Override
    public String getPrivateKey() {
        new BasicSSHUserPrivateKey(getScope(), getId(), getUsername(), toPrivateKeySource(secretValue), getPassphrase(), getDescription());

        return null;
    }

    @Override
    public Secret getPassphrase() {
        return NO_PASSPHRASE;
    }

    @NonNull
    @Override
    public List<String> getPrivateKeys() {
        return Collections.singletonList(this.getPrivateKey());
    }

    @NonNull
    @Override
    public String getUsername() {
        return username;
    }

    private static BasicSSHUserPrivateKey.PrivateKeySource toPrivateKeySource(String secret) throws IOException {
        final PEMParser pemParser = new PEMParser(new StringReader(secret));
        final Object object = pemParser.readObject();
        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        // NOTE: We do this all without passwords so we will only get an unencrypted key
        final KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);

        return new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(secret);
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        public DescriptorImpl() {
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.sshUserPrivateKey();
        }
    }
}
