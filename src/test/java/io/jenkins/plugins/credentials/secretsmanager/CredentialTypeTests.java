package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

/**
 * Ensure that all credential access methods are tested for all common credential types.
 */
public interface CredentialTypeTests {
    @Test
    void shouldSupportStringCredentials();

    @Test
    void shouldSupportUsernamePasswordCredentials();

    @Test
    void shouldSupportSshPrivateKeyCredentials();

    @Test
    void shouldSupportCertificateCredentials();
}
