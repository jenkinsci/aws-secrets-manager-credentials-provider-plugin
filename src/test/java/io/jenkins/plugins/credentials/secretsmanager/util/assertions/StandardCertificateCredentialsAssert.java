package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import hudson.util.Secret;

import java.util.Objects;

public class StandardCertificateCredentialsAssert extends StandardCredentialsAssert<StandardCertificateCredentials> {

    public StandardCertificateCredentialsAssert(StandardCertificateCredentials actual) {
        super(actual, StandardCertificateCredentialsAssert.class);
    }

    public StandardCertificateCredentialsAssert doesNotHavePassword() {
        isNotNull();

        if (!Objects.equals(actual.getPassword(), Secret.fromString(""))) {
            failWithMessage("Should not have password, but it was <%s>", actual.getPassword());
        }

        return this;
    }
}
