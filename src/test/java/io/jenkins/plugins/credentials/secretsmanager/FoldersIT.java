package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.FolderPluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.MyJenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


/**
 * The plugin should support folder-level configuration.
 */
public class FoldersIT {

    private static final String SECRET = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/folders/simple.yml")
    public void shouldHaveFolderLevelConfiguration() {
        // When
        final var folder = getFolder("foo");
        final var folderProperties = folder.getProperties();
        final var folderPluginConfiguration = folderProperties.get(FolderPluginConfiguration.class);
        final var actual = folderPluginConfiguration.getConfiguration().getClient().getEndpointConfiguration();

        // Then
        assertSoftly(s -> {
           s.assertThat(actual.getServiceEndpoint()).isEqualTo("https://example.com");
           s.assertThat(actual.getSigningRegion()).isEqualTo("us-east-1");
        });
    }

    @Test
    @ConfiguredWithCode(value = "/folders/merge.yml")
    public void shouldMergeFolderAndGlobalConfiguration() {
        fail("TODO test that folder-scoped config merges with and overrides global config");

        final EndpointConfiguration actual = null;

        // Then
        assertSoftly(s -> {
            // folder property overrides global
            s.assertThat(actual.getServiceEndpoint()).isEqualTo("https://example.com");
            // global property passes through unmodified
            s.assertThat(actual.getSigningRegion()).isEqualTo("us-east-1");
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportFolderScopedWithCredentialsBinding() {
        fail("TODO test if folder-scoped credentials work with `withCredentials` directive");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportFolderScopedEnvironmentBinding() {
        fail("TODO test if folder-scoped credentials work with `environment` directive");
    }

    private AbstractFolder<?> getFolder(String name) {
        var folder = jenkins.jenkins.getItem(name);

        if (folder instanceof AbstractFolder) {
            return (AbstractFolder<?>) folder;
        }
        return null;
    }
}
