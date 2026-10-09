package io.jenkins.plugins.credentials.secretsmanager;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents the folder scope for a credential.
 * A credential with folder scope is only accessible from jobs within the specified folders
 * and their subfolders (hierarchical matching).
 */
public class FolderScope implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(FolderScope.class.getName());
    private static final FolderScope GLOBAL = new FolderScope(Collections.emptySet());

    private final Set<String> folderPaths;

    private FolderScope(@NonNull Set<String> folderPaths) {
        this.folderPaths = Collections.unmodifiableSet(folderPaths);
    }

    /**
     * Creates a global scope (accessible from anywhere).
     *
     * @return a global FolderScope
     */
    @NonNull
    public static FolderScope global() {
        return GLOBAL;
    }

    /**
     * Creates a FolderScope from one or more folder paths.
     *
     * @param paths the folder paths
     * @return a FolderScope with the specified paths
     */
    @NonNull
    public static FolderScope of(@NonNull String... paths) {
        if (paths.length == 0) {
            return global();
        }

        Set<String> normalizedPaths = Arrays.stream(paths)
                .filter(Objects::nonNull)
                .map(FolderScope::normalizePath)
                .filter(path -> !path.isEmpty())
                .collect(Collectors.toSet());

        return normalizedPaths.isEmpty() ? global() : new FolderScope(normalizedPaths);
    }

    /**
     * Parses a comma-separated list of folder paths into a FolderScope.
     *
     * @param commaSeparated comma-separated folder paths (e.g., "engineering/backend,qa/staging")
     * @return a FolderScope with the parsed paths
     */
    @NonNull
    public static FolderScope parse(@CheckForNull String commaSeparated) {
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            LOG.log(Level.FINE, "Empty or null folder scope, treating as global");
            return global();
        }

        String[] paths = commaSeparated.split(",");
        Set<String> normalizedPaths = Arrays.stream(paths)
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .map(FolderScope::normalizePath)
                .filter(path -> !path.isEmpty())
                .collect(Collectors.toSet());

        if (normalizedPaths.isEmpty()) {
            LOG.log(Level.WARNING, "Could not parse any valid folder paths from: {0}", commaSeparated);
            return global();
        }

        LOG.log(Level.FINE, "Parsed folder scope: {0}", normalizedPaths);
        return new FolderScope(normalizedPaths);
    }

    /**
     * Checks if this scope is global (no folder restrictions).
     *
     * @return true if global, false otherwise
     */
    public boolean isGlobal() {
        return folderPaths.isEmpty();
    }

    /**
     * Checks if this credential is accessible from the specified requesting folder path.
     * Uses hierarchical matching: credentials are accessible from the specified folder and all child folders.
     *
     * @param requestingFolderPath the folder path from which access is requested (null means global/root context)
     * @return true if accessible, false otherwise
     */
    public boolean isAccessibleFrom(@CheckForNull String requestingFolderPath) {
        // Global scope is accessible from anywhere
        if (isGlobal()) {
            LOG.log(Level.FINER, "Global scope, accessible from anywhere");
            return true;
        }

        // Null or empty requesting path means global/root context
        // Folder-scoped credentials are NOT accessible from global context
        if (requestingFolderPath == null || requestingFolderPath.trim().isEmpty()) {
            LOG.log(Level.FINER, "Requesting from global context, folder-scoped credential not accessible");
            return false;
        }

        String normalizedRequestingPath = normalizePath(requestingFolderPath);
        if (normalizedRequestingPath.isEmpty()) {
            LOG.log(Level.FINER, "Normalized requesting path is empty, treating as global context");
            return false;
        }

        // Check if requesting path matches or is a child of any allowed path
        boolean accessible = folderPaths.stream()
                .anyMatch(allowedPath -> isChildOf(normalizedRequestingPath, allowedPath));

        LOG.log(Level.FINER, "Folder scope check: requesting={0}, allowed={1}, accessible={2}",
                new Object[]{normalizedRequestingPath, folderPaths, accessible});

        return accessible;
    }

    /**
     * Checks if the childPath is the same as or a child of the parentPath.
     * Uses hierarchical folder matching.
     *
     * @param childPath the path to check
     * @param parentPath the parent path
     * @return true if childPath equals or is a child of parentPath
     */
    private static boolean isChildOf(@NonNull String childPath, @NonNull String parentPath) {
        // Exact match
        if (childPath.equals(parentPath)) {
            return true;
        }

        // Child match: starts with parent and has a separator
        return childPath.startsWith(parentPath + "/");
    }

    /**
     * Normalizes a folder path by removing leading/trailing slashes and collapsing multiple slashes.
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    @NonNull
    private static String normalizePath(@CheckForNull String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        String normalized = path.trim();

        // Remove leading slashes
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Remove trailing slashes
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        // Collapse multiple consecutive slashes into one
        normalized = normalized.replaceAll("/+", "/");

        return normalized;
    }

    @NonNull
    public Set<String> getFolderPaths() {
        return folderPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderScope that = (FolderScope) o;
        return Objects.equals(folderPaths, that.folderPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderPaths);
    }

    @Override
    public String toString() {
        return isGlobal() ? "FolderScope{GLOBAL}" : "FolderScope{" + folderPaths + "}";
    }
}
