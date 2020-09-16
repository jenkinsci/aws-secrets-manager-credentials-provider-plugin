package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import io.jenkins.plugins.credentials.secretsmanager.config.transformer.TransformerTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class RemovePrefixTest implements TransformerTest {

    private static NameTransformer removePrefix(String prefix) {
        return new RemovePrefix(prefix);
    }

    @Test
    public void shouldAllowNullPrefix() {
        assertThat(removePrefix(null).transform("foo-secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldAllowEmptyPrefix() {
        assertThat(removePrefix("").transform("foo-secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldTrimWhitespaceFromPrefix() {
        assertThat(removePrefix(" foo- ").transform("foo-secret"))
                .isEqualTo("secret");
    }

    @Test
    public void shouldOnlyRemovePrefixFromStartOfString() {
        assertThat(removePrefix("-secret").transform("foo-secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldOnlyRemoveFirstOccurrenceOfPrefix() {
        assertThat(removePrefix("foo-").transform("foo-secret-foo-bar"))
                .isEqualTo("secret-foo-bar");
    }

    @Test
    public void shouldBeEqualWhenPrefixIsEqual() {
        final String prefix = "foo-";
        final NameTransformer a = removePrefix(prefix);

        assertSoftly(s -> {
            s.assertThat(a).as("Equal").isEqualTo(removePrefix(prefix));
            s.assertThat(a).as("Not Equal").isNotEqualTo(removePrefix(null));
        });
    }

    @Test
    public void shouldTransform() {
        assertThat(removePrefix("foo-").transform("foo-secret"))
                .isEqualTo("secret");
    }
}
