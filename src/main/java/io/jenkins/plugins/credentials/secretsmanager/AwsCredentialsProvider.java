package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.supplier.CredentialsSupplier;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Extension
public class AwsCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AwsCredentialsProvider.class.getName());

    private final Map<ModelObject, AwsCredentialsStore> stores = new ConcurrentHashMap<>();

    private final Supplier<Collection<StandardCredentials>> credentialsSupplier =
            memoizeWithExpiration(CredentialsSupplier.standard(), () ->
                    PluginConfiguration.normalize(PluginConfiguration.getInstance().getCache()));

    @Override
    @NonNull
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type,
                                                          ItemGroup itemGroup,
                                                          Authentication authentication) {
        if (ACL.SYSTEM.equals(authentication)) {
            Collection<StandardCredentials> allCredentials = Collections.emptyList();
            try {
                allCredentials = credentialsSupplier.get();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Could not list credentials in Secrets Manager: message=[{0}]", e.getMessage());
            }

            // Extract the requesting folder path
            final String requestingFolderPath = ItemGroups.getFolderPath(itemGroup);
            LOG.log(Level.FINE, "Filtering credentials for folder path: {0}",
                    requestingFolderPath != null ? requestingFolderPath : "<global>");

            return allCredentials.stream()
                    // Filter by folder scope
                    .filter(cred -> {
                        if (cred instanceof ScopedCredentials) {
                            ScopedCredentials scoped = (ScopedCredentials) cred;
                            boolean accessible = scoped.getFolderScope().isAccessibleFrom(requestingFolderPath);
                            if (!accessible) {
                                LOG.log(Level.FINER,
                                        "Credential {0} not accessible from folder {1}",
                                        new Object[]{scoped.getId(), requestingFolderPath});
                            }
                            return accessible;
                        }
                        // Non-scoped credentials are globally accessible (shouldn't happen with current implementation)
                        return true;
                    })
                    // Unwrap ScopedCredentials to get the actual credential
                    .map(cred -> {
                        if (cred instanceof ScopedCredentials) {
                            StandardCredentials delegate = ((ScopedCredentials) cred).getDelegate();
                            return delegate;
                        }
                        return cred;
                    })
                    .filter(c -> type.isAssignableFrom(c.getClass()))
                    // cast to keep generics happy even though we are assignable
                    .map(type::cast)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public CredentialsStore getStore(ModelObject object) {
        // Only create stores for Jenkins root and Folder objects (detect folders as ItemGroup+Item)
        if (object == Jenkins.get() || (object instanceof ItemGroup && object instanceof Item)) {
            return stores.computeIfAbsent(object, ctx -> new AwsCredentialsStore(this, ctx));
        }
        return null;
    }

    @Override
    public String getIconClassName() {
        return "icon-aws-secrets-manager-credentials-store";
    }

    private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Supplier<Duration> duration) {
        return CustomSuppliers.memoizeWithExpiration(base, duration);
    }
}