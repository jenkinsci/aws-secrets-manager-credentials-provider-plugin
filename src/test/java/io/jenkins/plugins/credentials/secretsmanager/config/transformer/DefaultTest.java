package io.jenkins.plugins.credentials.secretsmanager.config.transformer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTest implements TransformerTest {

    private static Transformer transformer() {
        return new Default();
    }

    @Test
    public void shouldTransform() {
        assertThat(transformer().transform("secret")).isEqualTo("secret");
    }
    @Test
    public void shouldInvert() {
        assertThat(transformer().inverse("secret")).isEqualTo("secret");
    }

    @Test
    public void shouldRoundTrip() {
        final String str = "foo-secret";
        final Transformer a = transformer();

        assertThat(a.inverse(a.transform(str))).isEqualTo(str);
    }
}
