package io.jenkins.plugins.credentials.secretsmanager;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Item;
import hudson.model.ItemGroup;
import jenkins.model.Jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for working with Jenkins ItemGroups, particularly for extracting folder paths.
 */
public abstract class ItemGroups {

    private static final Logger LOG = Logger.getLogger(ItemGroups.class.getName());

    private ItemGroups() {
        // Utility class
    }

    /**
     * Extract the full folder path from an ItemGroup.
     * <p>
     * Returns null for global/root contexts (null ItemGroup or Jenkins root).
     * Returns the folder path for folders and other ItemGroups.
     *
     * @param itemGroup the ItemGroup (may be null or Jenkins root)
     * @return the folder path (e.g., "engineering/backend"), or null if global context
     */
    @CheckForNull
    public static String getFolderPath(@CheckForNull ItemGroup<?> itemGroup) {
        if (itemGroup == null) {
            LOG.log(Level.FINE, "ItemGroup is null, treating as global context");
            return null;
        }

        // Jenkins.get() is the root - global context
        if (itemGroup instanceof Jenkins) {
            LOG.log(Level.FINE, "ItemGroup is Jenkins root, treating as global context");
            return null;
        }

        // Try to extract full name from Item interface
        if (itemGroup instanceof Item) {
            Item item = (Item) itemGroup;
            String fullName = item.getFullName();

            if (fullName != null && !fullName.isEmpty()) {
                String normalized = normalizePath(fullName);
                LOG.log(Level.FINE, "Extracted folder path from Item: {0}", normalized);
                return normalized;
            }
        }

        // Could not extract path - treat as global
        LOG.log(Level.FINE, "Could not extract folder path from ItemGroup {0}, treating as global context",
                itemGroup.getClass().getSimpleName());
        return null;
    }

    /**
     * Normalizes a folder path by removing leading/trailing slashes.
     *
     * @param path the path to normalize
     * @return the normalized path, or null if the input was null or empty
     */
    @CheckForNull
    private static String normalizePath(@CheckForNull String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
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

        return normalized.isEmpty() ? null : normalized;
    }
}
