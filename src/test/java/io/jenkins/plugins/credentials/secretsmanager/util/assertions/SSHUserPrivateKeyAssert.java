package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import hudson.util.Secret;

import java.util.List;
import java.util.Objects;

public class SSHUserPrivateKeyAssert extends StandardCredentialsAssert<SSHUserPrivateKey> {

    public SSHUserPrivateKeyAssert(SSHUserPrivateKey actual) {
        super(actual, SSHUserPrivateKeyAssert.class);
    }

    public SSHUserPrivateKeyAssert hasUsername(String username) {
        isNotNull();

        if (!Objects.equals(actual.getUsername(), username)) {
            failWithMessage("Expected username to be <%s> but was <%s>", username, actual.getUsername());
        }

        return this;
    }

    public SSHUserPrivateKeyAssert doesNotHavePassphrase() {
        return hasPassphrase(Secret.fromString(""));
    }

    public SSHUserPrivateKeyAssert hasPassphrase(Secret passphrase) {
        isNotNull();

        if (!Objects.equals(actual.getPassphrase(), passphrase)) {
            failWithMessage("Expected passphrase to be <%s> but was <%s>", passphrase, actual.getPassphrase());
        }

        return this;
    }

    public SSHUserPrivateKeyAssert hasPrivateKeys(List<String> privateKeys) {
        isNotNull();

        if (!Objects.equals(actual.getPrivateKeys(), privateKeys)) {
            failWithMessage("Expected private keys to be <%s> but was <%s>", privateKeys, actual.getPrivateKeys());
        }

        return this;
    }
}
