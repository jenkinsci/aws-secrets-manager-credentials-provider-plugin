package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

public interface CredentialsTests {
    @Test
    void shouldSupportStringCredentials();

    @Test
    void shouldSupportUsernamePasswordCredentials();

    @Test
    void shouldSupportSshPrivateKeyCredentials();

    @Test
    void shouldSupportCertificateCredentials();
}
