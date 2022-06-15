package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import io.jenkins.plugins.credentials.secretsmanager.config.transformer.TransformerTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTest implements TransformerTest {

    private static NameTransformer transformer() {
        return new Default();
    }

    @Test
    public void shouldTransform() {
        assertThat(transformer().transform("secret")).isEqualTo("secret");
    }
}
