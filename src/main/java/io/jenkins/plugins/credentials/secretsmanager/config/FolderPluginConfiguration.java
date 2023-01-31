package io.jenkins.plugins.credentials.secretsmanager.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Allows the plugin to be configured at the folder level (rather than the global level)
 */
public class FolderPluginConfiguration extends AbstractFolderProperty<AbstractFolder<?>> {

    private final PluginConfiguration configuration;

    public FolderPluginConfiguration() {
        this.configuration = null;
    }

    @DataBoundConstructor
    public FolderPluginConfiguration(PluginConfiguration configuration) {
        this.configuration = configuration;
    }

    public PluginConfiguration getConfiguration() {
        return configuration;
    }

    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {

    }
}
