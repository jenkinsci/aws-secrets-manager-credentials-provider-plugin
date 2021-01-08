package io.jenkins.plugins.credentials.secretsmanager.config.transformer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class RemovePrefixTest implements TransformerTest {

    private static Transformer removePrefix(String prefix) {
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

    /**
     * Do not allow regex prefixes, for compatibility with the SecretSource prefix removal feature.
     */
    @Test
    public void shouldNotAllowRegexPrefix() {
        assertThat(removePrefix("(.+)-").transform("foo-secret"))
                .isEqualTo("foo-secret");
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
        final Transformer a = removePrefix(prefix);

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

    @Test
    public void shouldInvert() {
        assertThat(removePrefix("foo-").inverse("secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldRoundTrip() {
        final String name = "foo-secret";
        final Transformer a = removePrefix("foo-");

        assertThat(a.inverse(a.transform(name))).isEqualTo(name);
    }
}
