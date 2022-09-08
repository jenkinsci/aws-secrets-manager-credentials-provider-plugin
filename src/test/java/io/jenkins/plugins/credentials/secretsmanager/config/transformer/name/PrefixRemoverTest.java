package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import org.assertj.core.util.Sets;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PrefixRemoverTest {

    private static PrefixRemover removePrefix(String prefix) {
        return PrefixRemover.removePrefix(prefix);
    }

    private static PrefixRemover removePrefixes(String... prefixes) {
        return PrefixRemover.removePrefixes(Sets.set(prefixes));
    }

    @Test
    public void shouldAllowNullPrefix() {
        assertThat(removePrefix(null).from("foo-secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldAllowEmptyPrefix() {
        assertThat(removePrefix("").from("foo-secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldTrimWhitespaceFromPrefix() {
        assertThat(removePrefix(" foo- ").from("foo-secret"))
                .isEqualTo("secret");
    }

    @Test
    public void shouldOnlyRemovePrefixFromStartOfString() {
        assertThat(removePrefix("-secret").from("foo-secret"))
                .isEqualTo("foo-secret");
    }

    @Test
    public void shouldOnlyRemoveFirstOccurrenceOfPrefix() {
        assertThat(removePrefix("foo-").from("foo-secret-foo-bar"))
                .isEqualTo("secret-foo-bar");
    }

    @Test
    public void shouldTransform() {
        assertThat(removePrefix("foo-").from("foo-secret"))
                .isEqualTo("secret");
    }

    @Test
    public void shouldTransformWithMultiplePrefixes() {
        final PrefixRemover a = removePrefixes("staging-", "production-");

        assertSoftly(s -> {
            s.assertThat(a.from("staging-secret")).isEqualTo("secret");
            s.assertThat(a.from("production-secret")).isEqualTo("secret");
            s.assertThat(a.from("secret")).isEqualTo("secret");
        });
    }

    @Test
    public void shouldNotRemoveMoreThanOnePrefixPerString() {
        assertThat(removePrefixes("staging-", "production-").from("staging-production-secret"))
                .isEqualTo("production-secret");
    }

    @Test
    public void shouldMatchTheMostSpecificPrefix() {
        assertThat(removePrefixes("aa", "aab").from("aab-secret"))
                .isEqualTo("secret");
    }
}
