package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import io.jenkins.plugins.credentials.secretsmanager.config.transformer.TransformerTest;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes.Prefix;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes.RemovePrefixes;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class RemovePrefixesTest implements TransformerTest {

    @Test
    public void shouldTransform() {
        final var transformer = removePrefixes("foo-", "bar-");

        assertSoftly(s -> {
            s.assertThat(transformer.transform("foo-secret")).isEqualTo("secret");
            s.assertThat(transformer.transform("bar-secret")).isEqualTo("secret");
        });
    }

    @Test
    public void shouldBeEqualWhenPrefixesAreEqual() {
        final var a = removePrefixes("foo-", "bar-");

        assertSoftly(s -> {
            s.assertThat(a).as("Equal").isEqualTo(removePrefixes("foo-", "bar-"));
            s.assertThat(a).as("Not Equal").isNotEqualTo(removePrefixes("bar-"));
        });
    }

    private static NameTransformer removePrefixes(String... prefixes) {
        final var prefixSet = Arrays.stream(prefixes)
                .map(Prefix::new)
                .collect(Collectors.toSet());

        return new RemovePrefixes(prefixSet);
    }
}
