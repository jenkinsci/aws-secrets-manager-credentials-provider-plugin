package io.jenkins.plugins.credentials.secretsmanager.factory;

import io.jenkins.plugins.credentials.secretsmanager.FolderScope;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for how the folder scope is built from a secret's tags.
 */
public class FolderTagsTest {

    private static final String FOLDER = "jenkins:credentials:folder";

    private static FolderScope scope(String... keyValues) {
        final Map<String, String> tags = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            tags.put(keyValues[i], keyValues[i + 1]);
        }
        return CredentialsFactory.folderScope(tags);
    }

    @Test
    public void shouldBeGlobalWithoutFolderTags() {
        assertThat(scope("jenkins:credentials:type", "string").isGlobal()).isTrue();
    }

    @Test
    public void shouldKeepSingleTagBehaviour() {
        assertThat(scope(FOLDER, "engineering/backend").getFolderPaths()).containsExactly("engineering/backend");
    }

    @Test
    public void shouldAddFoldersFromAdditionalTagKeys() {
        final FolderScope s = scope(
                FOLDER, "engineering/backend",
                FOLDER + ":2", "engineering/frontend",
                FOLDER + ":qa", "qa");

        assertThat(s.getFolderPaths()).containsExactlyInAnyOrder("engineering/backend", "engineering/frontend", "qa");
        assertThat(s.isAccessibleFrom("engineering/frontend/web")).isTrue();
        assertThat(s.isAccessibleFrom("engineering")).isFalse();
        assertThat(s.isAccessibleFrom("ops")).isFalse();
        assertThat(s.isAccessibleFrom(null)).isFalse();
    }

    @Test
    public void shouldScopeWithAdditionalKeysOnly() {
        final FolderScope s = scope(FOLDER + ":2", "engineering/backend");

        assertThat(s.isGlobal()).isFalse();
        assertThat(s.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(s.isAccessibleFrom("qa")).isFalse();
    }

    @Test
    public void shouldIgnoreTagsThatOnlyLookSimilar() {
        final FolderScope s = scope(
                FOLDER, "engineering/backend",
                "jenkins:credentials:folders", "ops",
                "jenkins:credentials:folder2", "ops",
                "jenkins:credentials:folder.2", "ops");

        assertThat(s.getFolderPaths()).containsExactly("engineering/backend");
    }
}
