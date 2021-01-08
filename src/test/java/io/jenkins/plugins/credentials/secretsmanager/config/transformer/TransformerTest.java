package io.jenkins.plugins.credentials.secretsmanager.config.transformer;

import org.junit.Test;

public interface TransformerTest {

    @Test
    void shouldTransform();

    @Test
    void shouldInvert();

    @Test
    void shouldRoundTrip();
}
