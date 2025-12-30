package io.jenkins.plugins.credentials.secretsmanager.supplier;

import software.amazon.awssdk.services.secretsmanager.model.Tag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ListsTest {

    // toMap //

    @Test
    public void shouldTransformNullListToMap() {
        assertThat(Lists.toMap(null, Tag::key, Tag::value))
                .isEmpty();
    }

    @Test
    public void shouldTransformEmptyListToMap() {
        final List<Tag> tags = Collections.emptyList();

        assertThat(Lists.toMap(tags, Tag::key, Tag::value))
                .isEmpty();
    }

    @Test
    public void shouldTransformListToMap() {
        final var tags = List.of(
                newTag("foo", "1"),
                newTag("bar", "2"));

        assertThat(Lists.toMap(tags, Tag::key, Tag::value))
                .containsOnly(entry("foo", "1"), entry("bar", "2"));
    }

    @Test
    public void shouldNotTransformListWithDuplicateKeysToMap() {
        final var tags = List.of(
                newTag("foo", "3"),
                newTag("foo", "2"));

        assertThatIllegalStateException()
                .isThrownBy(() -> Lists.toMap(tags, Tag::key, Tag::value));
    }

    private static Tag newTag(String key, String value) {
        return Tag.builder()
                .key(key)
                .value(value)
                .build();
    }
}
