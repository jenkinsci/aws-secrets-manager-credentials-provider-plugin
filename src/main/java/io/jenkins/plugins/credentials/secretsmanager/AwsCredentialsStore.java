package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.domains.Domain;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.ModelObject;
import hudson.security.ACL;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class AwsCredentialsStore extends CredentialsStore {

    private final AwsCredentialsProvider provider;
    private final AwsCredentialsStoreAction action = new AwsCredentialsStoreAction(this);

    public AwsCredentialsStore(AwsCredentialsProvider provider) {
        super(AwsCredentialsProvider.class);
        this.provider = provider;
    }

    @Nonnull
    @Override
    public ModelObject getContext() {
        return Jenkins.get();
    }

    @Override
    public boolean hasPermission(@NonNull Authentication authentication,
                                 @NonNull Permission permission) {
        return CredentialsProvider.VIEW.equals(permission)
                && Jenkins.get().getACL().hasPermission(authentication, permission);
    }

    @Nonnull
    @Override
    public List<Credentials> getCredentials(@NonNull Domain domain) {
        // Only the global domain is supported
        if (Domain.global().equals(domain)
                && Jenkins.get().hasPermission(CredentialsProvider.VIEW)) {
            return provider.getCredentials(Credentials.class, Jenkins.get(), ACL.SYSTEM);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean addCredentials(@Nonnull Domain domain, @Nonnull Credentials credentials) {
        throw new UnsupportedOperationException(
                "Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Override
    public boolean removeCredentials(@NonNull Domain domain, @NonNull Credentials credentials) {
        throw new UnsupportedOperationException(
                "Jenkins may not remove credentials from AWS Secrets Manager");
    }

    @Override
    public boolean updateCredentials(@NonNull Domain domain, @NonNull Credentials current,
                                     @NonNull Credentials replacement) {
        throw new UnsupportedOperationException(
                "Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Nullable
    @Override
    public CredentialsStoreAction getStoreAction() {
        return action;
    }

}
