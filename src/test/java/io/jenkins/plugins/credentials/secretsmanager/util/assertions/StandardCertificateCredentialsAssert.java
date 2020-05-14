package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import hudson.util.Secret;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class StandardCertificateCredentialsAssert extends AbstractAssert<StandardCertificateCredentialsAssert, StandardCertificateCredentials> {

    public StandardCertificateCredentialsAssert(StandardCertificateCredentials actual) {
        super(actual, StandardCertificateCredentialsAssert.class);
    }

    public StandardCertificateCredentialsAssert hasPassword(Secret password) {
        isNotNull();

        if (!Objects.equals(actual.getPassword(), password)) {
            failWithMessage("Expected password to be <%s> but was <%s>", password, actual.getPassword());
        }

        return this;
    }

    public StandardCertificateCredentialsAssert doesNotHavePassword() {
        isNotNull();

        if (!Objects.equals(actual.getPassword(), Secret.fromString(""))) {
            failWithMessage("Should not have password, but it was <%s>", actual.getPassword());
        }

        return this;
    }

    public StandardCertificateCredentialsAssert hasId(String id) {
        new StandardCredentialsAssert(actual).hasId(id);

        return this;
    }

    public StandardCertificateCredentialsAssert hasSameDescriptorIconAs(StandardCertificateCredentials theirs) {
        new StandardCredentialsAssert(actual).hasSameDescriptorIconAs(theirs);

        return this;
    }
}
