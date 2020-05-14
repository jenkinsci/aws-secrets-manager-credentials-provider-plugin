package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import org.assertj.core.api.SoftAssertionsProvider;

public interface StandardCertificateCredentialsSoftAssertionsProvider extends SoftAssertionsProvider {
    default StandardCertificateCredentialsAssert assertThat(StandardCertificateCredentials actual) {
        return proxy(StandardCertificateCredentialsAssert.class, StandardCertificateCredentials.class, actual);
    }
}
