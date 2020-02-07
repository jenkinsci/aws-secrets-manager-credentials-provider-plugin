package io.jenkins.plugins.credentials.secretsmanager.supplier;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalsTest {
    @Test
    public void shouldStreamOptional() {
        assertThat(Optionals.stream(Optional.of("foo")))
                .containsExactly("foo");
    }

    @Test
    public void shouldStreamEmptyOptional() {
        assertThat(Optionals.stream(Optional.empty()))
                .isEmpty();
    }
}
