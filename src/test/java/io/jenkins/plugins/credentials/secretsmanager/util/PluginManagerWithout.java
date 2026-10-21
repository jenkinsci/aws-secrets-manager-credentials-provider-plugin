package io.jenkins.plugins.credentials.secretsmanager.util;

import hudson.Util;
import org.jvnet.hudson.test.UnitTestSupportingPluginManager;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * A test Jenkins plugin manager that does not install the given plugins, simulating a Jenkins where they are absent.
 * <p>
 * Pass it to {@link org.jvnet.hudson.test.JenkinsRule#with(hudson.PluginManager)} before the rule starts.
 * <p>
 * Note this only affects what Jenkins considers installed. The excluded plugins' jars are still on the test
 * classpath, because they are (optional) dependencies of this plugin.
 */
public class PluginManagerWithout extends UnitTestSupportingPluginManager {

    private final Set<String> excludedShortNames;

    public PluginManagerWithout(String... shortNames) throws IOException {
        super(Util.createTempDir());
        this.excludedShortNames = Set.of(shortNames);
    }

    @Override
    protected void copyBundledPlugin(URL src, String fileName) throws IOException {
        final boolean excluded = excludedShortNames.stream().anyMatch(name -> fileName.startsWith(name + "."));
        if (!excluded) {
            super.copyBundledPlugin(src, fileName);
        }
    }
}
