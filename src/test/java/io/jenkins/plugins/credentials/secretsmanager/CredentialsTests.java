package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

public interface CredentialsTests {
    @Test
    void shouldHaveName();

    @Test
    void shouldAppearInCredentialsProvider();

    @Test
    void shouldSupportWithCredentialsBinding();

    @Test
    void shouldSupportEnvironmentBinding();

    @Test
    void shouldSupportSnapshots();
}
