package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Expose the store.
 */
@ExportedBean
public class AwsCredentialsStoreAction extends CredentialsStoreAction {

    private static final String ICON_CLASS = "icon-aws-secrets-manager-credentials-store";

    private final AwsCredentialsStore store;

    AwsCredentialsStoreAction(AwsCredentialsStore store) {
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
