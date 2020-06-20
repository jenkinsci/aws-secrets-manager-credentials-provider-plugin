package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import org.assertj.core.api.SoftAssertionsProvider;

public interface StandardCredentialsSoftAssertionsProvider extends SoftAssertionsProvider {
    default StandardCredentialsAssert assertThat(StandardCredentials actual) {
        return proxy(StandardCredentialsAssert.class, StandardCredentials.class, actual);
    }
}
