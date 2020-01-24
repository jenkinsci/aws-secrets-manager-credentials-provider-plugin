package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

public interface CredentialsTests {
    @Test
    void shouldSupportListView();

    @Test
    void shouldHaveDescriptorIcon();

    @Test
    void shouldSupportWithCredentialsBinding();

    @Test
    void shouldSupportEnvironmentBinding();

    @Test
    void shouldSupportSnapshots();

    @Test
    void shouldHaveId();
}
