package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.domains.Domain;

import org.acegisecurity.Authentication;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.ModelObject;
import hudson.security.ACL;
import hudson.security.Permission;
import jenkins.model.Jenkins;

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

    /**
     * Expose the store.
     */
    @ExportedBean
    public static class AwsCredentialsStoreAction extends CredentialsStoreAction {

        private static final String ICON_CLASS = "icon-aws-secrets-manager-credentials-store";

        private final AwsCredentialsStore store;

        private AwsCredentialsStoreAction(AwsCredentialsStore store) {
            this.store = store;
            addIcons();
        }

        private void addIcons() {
            IconSet.icons.addIcon(new Icon(ICON_CLASS + " icon-sm",
                    "aws-secrets-manager-credentials-provider/images/16x16/icon.png",
                    Icon.ICON_SMALL_STYLE, IconType.PLUGIN));
            IconSet.icons.addIcon(new Icon(ICON_CLASS + " icon-md",
                    "aws-secrets-manager-credentials-provider/images/24x24/icon.png",
                    Icon.ICON_MEDIUM_STYLE, IconType.PLUGIN));
            IconSet.icons.addIcon(new Icon(ICON_CLASS + " icon-lg",
                    "aws-secrets-manager-credentials-provider/images/32x32/icon.png",
                    Icon.ICON_LARGE_STYLE, IconType.PLUGIN));
            IconSet.icons.addIcon(new Icon(ICON_CLASS + " icon-xlg",
                    "aws-secrets-manager-credentials-provider/images/48x48/icon.png",
                    Icon.ICON_XLARGE_STYLE, IconType.PLUGIN));
        }

        @Override
        @NonNull
        public CredentialsStore getStore() {
            return store;
        }

        @Override
        public String getIconFileName() {
            return isVisible()
                    ? "/plugin/aws-secrets-manager-credentials-provider/images/32x32/icon.png"
                    : null;
        }

        @Override
        public String getIconClassName() {
            return isVisible()
                    ? ICON_CLASS
                    : null;
        }

        @Override
        public String getDisplayName() {
            return Messages.awsSecretsManager();
        }
    }
}
