package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.SdkBaseException;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.base.Suppliers;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import io.jenkins.plugins.credentials.secretsmanager.supplier.CredentialsSupplier;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Extension
public class AwsCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AwsCredentialsProvider.class.getName());

    private final AwsCredentialsStore store = new AwsCredentialsStore(this);

    private final Supplier<Collection<StandardCredentials>> credentialsSupplier =
            memoizeWithExpiration(CredentialsSupplier.standard(), Duration.ofMinutes(5));

    @Override
    @NonNull
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type,
                                                          ItemGroup itemGroup,
                                                          Authentication authentication) {
        if (ACL.SYSTEM.equals(authentication)) {
            Collection<StandardCredentials> allCredentials = Collections.emptyList();
            try {
                allCredentials = credentialsSupplier.get();
            } catch (SdkBaseException e) {
                LOG.log(Level.WARNING, "Could not list credentials in Secrets Manager: message=[{0}]", e.getMessage());
            }

            return allCredentials.stream()
                    .filter(c -> type.isAssignableFrom(c.getClass()))
                    // cast to keep generics happy even though we are assignable
                    .map(type::cast)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public CredentialsStore getStore(ModelObject object) {
        return object == Jenkins.getInstance() ? store : null;
    }

    @Override
    public String getIconClassName() {
        return "icon-aws-secrets-manager-credentials-store";
    }

    private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Duration duration) {
        return Suppliers.memoizeWithExpiration(base::get, duration.toMillis(), TimeUnit.MILLISECONDS)::get;
    }
}