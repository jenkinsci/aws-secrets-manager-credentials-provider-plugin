package io.jenkins.plugins.credentials.secretsmanager.config.migrations;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonHomeLoader;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.net.URL;

/**
 * Defines migration tests for the Jenkins plugin configuration data model.
 * <p>The format is loosely based on Ruby On Rails' <a href="https://guides.rubyonrails.org/v5.1/active_record_migrations.html">ActiveRecord Migrations</a>. Like ActiveRecord Migrations, it uses naming conventions, and a single <code>change()</code> method per test case. Unlike ActiveRecord Migrations, there is no concept of migrating backwards as we do not support plugin downgrades, so we only need to test the forward path.</p>
 * <h2>Usage</h2>
 * <ol>
 *   <li>Create a test case that extends this class, following the naming conventions.</li>
 *   <li>Implement {@link #change(PluginConfiguration)} to assert that the property is migrated correctly.</li>
 *   <li>Provide an example of the legacy plugin XML configuration that will be migrated under <code>src/test/resources/path/to/migration/test/</code>. For example <code>src/test/resources/io/jenkins/plugins/thing/config/migrations/ChangeFooBarToFooBarsTest/io.jenkins.plugins.thing.config.PluginConfiguration.xml</code>.</li>
 *   <li>Run test.</li>
 * </ol>
 * <h2>Naming Conventions</h2>
 * <p>Use ActiveRecord Migration style naming conventions, plus a <code>Test</code> suffix, to indicate what kind of migration you are testing.</p>
 * <p>Examples:</p>
 * <ul>
 *   <li><code>AddBarToFooTest</code>: This would test that the <code>bar</code> property is added to the <code>foo</code> config. I.e. that <code>PluginConfiguration.getFoo().getBar()</code> exists. (This is only necessary when the new property is mandatory and can be populated with a default value.)</li>
 *   <li><code>ChangeFooBarToFooBarsTest</code>: This would test that the <code>bar</code> property on <code>foo</code> has been renamed to <code>bars</code>. (This also normally implies a data type change from a single object to a list.)</li>
 *   <li><code>RemoveBarsFromFooTest</code>: This would test that the <code>bars</code> property has been removed from <code>foo</code>.</li>
 * </ul>
 */
public abstract class MigrationTest {

    @Rule
    public final JenkinsRule jenkins = new JenkinsRule()
            .with(new LocalClass(this.getClass()));

    /**
     * Implement this to assert that the property in question has been migrated correctly.
     *
     * @param config The config map in which the property will be found, or not found if it was removed.
     */
    protected abstract void change(PluginConfiguration config);

    @Test
    public void shouldMigrate() {
        final PluginConfiguration config = getPluginConfiguration();
        change(config);
    }

    private PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) jenkins.getInstance().getDescriptor(PluginConfiguration.class);
    }

    /**
     * Adaptation of Local for when we just want to load one Hudson home per class, rather than one per test.
     */
    private static class LocalClass implements HudsonHomeLoader {

        private final Class<?> testClass;

        public LocalClass(Class<?> testClass) {
            this.testClass = testClass;
        }

        @Override
        public File allocate() throws Exception {
            URL res = findDataResource();
            if(!res.getProtocol().equals("file"))
                throw new AssertionError("Test data is not available in the file system: "+res);
            File home = new File(res.toURI());
            System.err.println("Loading $JENKINS_HOME from " + home);

            return new CopyExisting(home).allocate();
        }

        private URL findDataResource() {
            for( String suffix : SUFFIXES ) {
                URL res = testClass.getResource(testClass.getSimpleName() + suffix);
                if(res!=null)   return res;
            }

            throw new AssertionError("No test resource was found for "+testClass);
        }

        private static final String[] SUFFIXES = {"/", ".zip"};
    }
}
