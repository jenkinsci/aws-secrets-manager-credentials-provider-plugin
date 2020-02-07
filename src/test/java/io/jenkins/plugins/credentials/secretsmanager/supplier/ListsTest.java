package io.jenkins.plugins.credentials.secretsmanager.supplier;

import com.amazonaws.services.secretsmanager.model.Tag;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ListsTest {

    // Concat //

    @Test
    public void shouldConcat() {
        assertThat(Lists.concat("foo", Arrays.asList("bar", "baz")))
                .containsExactly("foo", "bar", "baz");
    }

    @Test
    public void shouldConcatToEmptyList() {
        assertThat(Lists.concat("foo", Collections.emptyList()))
                .containsExactly("foo");
    }

    // toMap //

    @Test
    public void shouldTransformNullListToMap() {
        assertThat(Lists.toMap(null, Tag::getKey, Tag::getValue))
                .isEmpty();
    }

    @Test
    public void shouldTransformEmptyListToMap() {
        final List<Tag> tags = Collections.emptyList();

        assertThat(Lists.toMap(tags, Tag::getKey, Tag::getValue))
                .isEmpty();
    }

    @Test
    public void shouldTransformListToMap() {
        final List<Tag> tags = com.google.common.collect.Lists.newArrayList(
                newTag("foo", "1"),
                newTag("bar", "2"));

        assertThat(Lists.toMap(tags, Tag::getKey, Tag::getValue))
                .containsOnly(entry("foo", "1"), entry("bar", "2"));
    }

    @Test
    public void shouldNotTransformListWithDuplicateKeysToMap() {
        final List<Tag> tags = com.google.common.collect.Lists.newArrayList(
                newTag("foo", "3"),
                newTag("foo", "2"));

        assertThatIllegalStateException()
                .isThrownBy(() -> Lists.toMap(tags, Tag::getKey, Tag::getValue));
    }

    private static Tag newTag(String key, String value) {
        return new Tag().withKey(key).withValue(value);
    }
}
