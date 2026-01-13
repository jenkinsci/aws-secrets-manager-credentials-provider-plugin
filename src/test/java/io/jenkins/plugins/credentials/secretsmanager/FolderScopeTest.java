package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FolderScope.
 */
public class FolderScopeTest {

    @Test
    public void testGlobalScope() {
        FolderScope global = FolderScope.global();

        assertThat(global.isGlobal()).isTrue();
        assertThat(global.isAccessibleFrom(null)).isTrue();
        assertThat(global.isAccessibleFrom("")).isTrue();
        assertThat(global.isAccessibleFrom("engineering")).isTrue();
        assertThat(global.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(global.isAccessibleFrom("qa")).isTrue();
    }

    @Test
    public void testExactMatch() {
        FolderScope scope = FolderScope.of("engineering/backend");

        assertThat(scope.isGlobal()).isFalse();
        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
    }

    @Test
    public void testChildFolderAccess() {
        FolderScope scope = FolderScope.of("engineering/backend");

        // Child folders should have access
        assertThat(scope.isAccessibleFrom("engineering/backend/api")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/backend/services")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/backend/api/v1")).isTrue();
    }

    @Test
    public void testParentFolderDenied() {
        FolderScope scope = FolderScope.of("engineering/backend");

        // Parent folders should NOT have access
        assertThat(scope.isAccessibleFrom("engineering")).isFalse();
        assertThat(scope.isAccessibleFrom(null)).isFalse();
        assertThat(scope.isAccessibleFrom("")).isFalse();
    }

    @Test
    public void testSiblingFolderDenied() {
        FolderScope scope = FolderScope.of("engineering/backend");

        // Sibling folders should NOT have access
        assertThat(scope.isAccessibleFrom("engineering/frontend")).isFalse();
        assertThat(scope.isAccessibleFrom("qa")).isFalse();
        assertThat(scope.isAccessibleFrom("qa/staging")).isFalse();
    }

    @Test
    public void testMultipleFolders() {
        FolderScope scope = FolderScope.of("engineering/backend", "qa/staging");

        // Should be accessible from both folders
        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/backend/api")).isTrue();
        assertThat(scope.isAccessibleFrom("qa/staging")).isTrue();
        assertThat(scope.isAccessibleFrom("qa/staging/tests")).isTrue();

        // Should NOT be accessible from other folders
        assertThat(scope.isAccessibleFrom("engineering")).isFalse();
        assertThat(scope.isAccessibleFrom("engineering/frontend")).isFalse();
        assertThat(scope.isAccessibleFrom("qa")).isFalse();
        assertThat(scope.isAccessibleFrom("qa/production")).isFalse();
    }

    @Test
    public void testParseCommaSeparated() {
        FolderScope scope = FolderScope.parse("engineering/backend,qa/staging");

        assertThat(scope.isGlobal()).isFalse();
        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(scope.isAccessibleFrom("qa/staging")).isTrue();
    }

    @Test
    public void testParseCommaSeparatedWithWhitespace() {
        FolderScope scope = FolderScope.parse("engineering/backend , qa/staging , prod/deployment");

        assertThat(scope.isGlobal()).isFalse();
        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(scope.isAccessibleFrom("qa/staging")).isTrue();
        assertThat(scope.isAccessibleFrom("prod/deployment")).isTrue();
    }

    @Test
    public void testParseNull() {
        FolderScope scope = FolderScope.parse(null);

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    public void testParseEmpty() {
        FolderScope scope = FolderScope.parse("");

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    public void testParseEmptyPaths() {
        FolderScope scope = FolderScope.parse(",,");

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    public void testNormalizePathsWithLeadingSlash() {
        FolderScope scope = FolderScope.of("/engineering/backend");

        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
    }

    @Test
    public void testNormalizePathsWithTrailingSlash() {
        FolderScope scope = FolderScope.of("engineering/backend/");

        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
    }

    @Test
    public void testNormalizePathsWithBothSlashes() {
        FolderScope scope = FolderScope.of("/engineering/backend/");

        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
    }

    @Test
    public void testNormalizeRequestingPath() {
        FolderScope scope = FolderScope.of("engineering/backend");

        assertThat(scope.isAccessibleFrom("/engineering/backend")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/backend/")).isTrue();
        assertThat(scope.isAccessibleFrom("/engineering/backend/")).isTrue();
    }

    @Test
    public void testEmptyPathTreatedAsGlobal() {
        FolderScope scope = FolderScope.of("");

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    public void testMultipleSlashesCollapsed() {
        FolderScope scope = FolderScope.of("engineering//backend");

        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/backend/api")).isTrue();
    }

    @Test
    public void testPartialMatch() {
        FolderScope scope = FolderScope.of("engineering/backend");

        // "engineering/backend-new" should NOT match
        assertThat(scope.isAccessibleFrom("engineering/backend-new")).isFalse();
    }

    @Test
    public void testSingleLevelFolder() {
        FolderScope scope = FolderScope.of("engineering");

        assertThat(scope.isAccessibleFrom("engineering")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/backend")).isTrue();
        assertThat(scope.isAccessibleFrom("engineering/frontend")).isTrue();
        assertThat(scope.isAccessibleFrom("qa")).isFalse();
    }

    @Test
    public void testGetFolderPaths() {
        FolderScope scope = FolderScope.of("engineering/backend", "qa/staging");

        assertThat(scope.getFolderPaths())
                .containsExactlyInAnyOrder("engineering/backend", "qa/staging");
    }

    @Test
    public void testEquals() {
        FolderScope scope1 = FolderScope.of("engineering/backend");
        FolderScope scope2 = FolderScope.of("engineering/backend");
        FolderScope scope3 = FolderScope.of("engineering/frontend");

        assertThat(scope1).isEqualTo(scope2);
        assertThat(scope1).isNotEqualTo(scope3);
    }

    @Test
    public void testHashCode() {
        FolderScope scope1 = FolderScope.of("engineering/backend");
        FolderScope scope2 = FolderScope.of("engineering/backend");

        assertThat(scope1.hashCode()).isEqualTo(scope2.hashCode());
    }

    @Test
    public void testToString() {
        FolderScope global = FolderScope.global();
        FolderScope scoped = FolderScope.of("engineering/backend");

        assertThat(global.toString()).contains("GLOBAL");
        assertThat(scoped.toString()).contains("engineering/backend");
    }
}
