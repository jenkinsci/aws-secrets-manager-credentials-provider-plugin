package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.TransformerTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class RemovePrefixesTest implements TransformerTest {

    @Test
    public void shouldTransform() {
        final NameTransformer transformer = removePrefixes("foo-", "bar-");

        assertThat(transformer.transform("foo-secret"))
                .isEqualTo("secret");
    }

    @Test
    public void shouldBeEqualWhenPrefixesAreEqual() {
        final NameTransformer a = removePrefixes("foo-", "bar-");

        assertSoftly(s -> {
            s.assertThat(a).as("Equal").isEqualTo(removePrefixes("foo-", "bar-"));
            s.assertThat(a).as("Not Equal").isNotEqualTo(removePrefixes("bar-"));
        });
    }

    private static NameTransformer removePrefixes(String... prefixes) {
        final Set<Value> prefixSet = Arrays.stream(prefixes)
                .map(Value::new)
                .collect(Collectors.toSet());

        return new RemovePrefixes(prefixSet);
    }
}
