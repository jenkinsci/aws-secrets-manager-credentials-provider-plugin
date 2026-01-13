package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ItemGroups utility.
 * Note: Most functionality is tested via integration tests using real Folder objects.
 */
public class ItemGroupsTest {

    @Test
    public void testNullItemGroup() {
        String path = ItemGroups.getFolderPath(null);

        assertThat(path).isNull();
    }
}
