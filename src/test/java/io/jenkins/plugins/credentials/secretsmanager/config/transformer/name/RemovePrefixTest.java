package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import io.jenkins.plugins.credentials.secretsmanager.config.transformer.TransformerTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class RemovePrefixTest implements TransformerTest {

    @Test
    public void shouldTransform() {
        final var transformer = new RemovePrefix("foo-");

        assertThat(transformer.transform("foo-secret"))
                .isEqualTo("secret");
    }

    @Test
    public void shouldBeEqualWhenPrefixIsEqual() {
        final var a = new RemovePrefix("foo-");

        assertSoftly(s -> {
            s.assertThat(a).as("Equal").isEqualTo(new RemovePrefix("foo-"));
            s.assertThat(a).as("Not Equal").isNotEqualTo(new RemovePrefix("bar-"));
        });
    }
}
