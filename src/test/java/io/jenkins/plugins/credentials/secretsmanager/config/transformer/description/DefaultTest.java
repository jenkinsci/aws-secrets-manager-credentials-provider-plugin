package io.jenkins.plugins.credentials.secretsmanager.config.transformer.description;

import io.jenkins.plugins.credentials.secretsmanager.config.transformer.TransformerTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTest implements TransformerTest {

    private static DescriptionTransformer transformer() {
        return new Default();
    }

    @Test
    public void shouldTransform() {
        assertThat(transformer().transform("foobar")).isEqualTo("foobar");
    }
}
