package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

/**
 * Wrapper that adds folder scope metadata to any StandardCredentials.
 * This allows credentials to be filtered based on the requesting folder context
 * without modifying the individual credential type implementations.
 */
public class ScopedCredentials implements StandardCredentials {

    private final StandardCredentials delegate;
    private final FolderScope folderScope;

    /**
     * Creates a scoped credential wrapper.
     *
     * @param delegate the underlying credential
     * @param folderScope the folder scope for this credential
     */
    public ScopedCredentials(@NonNull StandardCredentials delegate, @NonNull FolderScope folderScope) {
        this.delegate = delegate;
        this.folderScope = folderScope;
    }

    /**
     * Gets the folder scope for this credential.
     *
     * @return the folder scope
     */
    @NonNull
    public FolderScope getFolderScope() {
        return folderScope;
    }

    /**
     * Gets the underlying credential cast to the specified type.
     *
     * @param type the type to cast to
     * @param <T> the credential type
     * @return the credential as the specified type, or null if not compatible
     */
    @CheckForNull
    public <T> T as(@NonNull Class<T> type) {
        if (type.isInstance(delegate)) {
            return type.cast(delegate);
        }
        return null;
    }

    /**
     * Gets the underlying delegate credential.
     *
     * @return the delegate credential
     */
    @NonNull
    public StandardCredentials getDelegate() {
        return delegate;
    }

    // Delegate all StandardCredentials methods to the underlying credential

    @NonNull
    @Override
    public String getId() {
        return delegate.getId();
    }

    @NonNull
    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @NonNull
    @Override
    public CredentialsScope getScope() {
        // Note: This is Jenkins' CredentialsScope enum (GLOBAL/SYSTEM), not FolderScope
        return Objects.requireNonNullElse(delegate.getScope(), CredentialsScope.GLOBAL);
    }

    @NonNull
    @Override
    public com.cloudbees.plugins.credentials.CredentialsDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScopedCredentials that = (ScopedCredentials) o;

        if (!delegate.equals(that.delegate)) return false;
        return folderScope.equals(that.folderScope);
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + folderScope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ScopedCredentials{" +
                "id=" + getId() +
                ", folderScope=" + folderScope +
                ", delegate=" + delegate.getClass().getSimpleName() +
                '}';
    }
}
