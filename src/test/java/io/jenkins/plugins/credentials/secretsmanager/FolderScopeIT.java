package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.security.ACL;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.Tag;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for folder-based credential scoping.
 */
public class FolderScopeIT {

    private static final String SECRET = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldMakeGlobalCredentialAccessibleEverywhere() throws IOException {
        // Given: A credential without folder tag (global)
        final var secret = createStringSecret(SECRET);

        // Create folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var qaFolder = jenkins.jenkins.createProject(Folder.class, "qa");

        // When: Look up credentials from different contexts
        final var globalCredentials = lookupCredentials(jenkins.jenkins);
        final var engineeringCredentials = lookupCredentials(engineeringFolder);
        final var qaCredentials = lookupCredentials(qaFolder);

        // Then: Global credential is accessible everywhere
        assertThat(globalCredentials).extracting("id").contains(secret.name());
        assertThat(engineeringCredentials).extracting("id").contains(secret.name());
        assertThat(qaCredentials).extracting("id").contains(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldMakeFolderCredentialAccessibleInFolder() throws IOException {
        // Given: A credential with folder tag
        final var secret = createStringSecretWithFolder(SECRET, "engineering");

        // Create folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var qaFolder = jenkins.jenkins.createProject(Folder.class, "qa");

        // When: Look up credentials from different contexts
        final var engineeringCredentials = lookupCredentials(engineeringFolder);
        final var qaCredentials = lookupCredentials(qaFolder);

        // Then: Credential is only accessible in engineering folder
        assertThat(engineeringCredentials).extracting("id").contains(secret.name());
        assertThat(qaCredentials).extracting("id").doesNotContain(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldMakeFolderCredentialAccessibleInChildFolder() throws IOException {
        // Given: A credential scoped to parent folder
        final var secret = createStringSecretWithFolder(SECRET, "engineering");

        // Create nested folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var backendFolder = engineeringFolder.createProject(Folder.class, "backend");

        // When: Look up credentials from child folder
        final var backendCredentials = lookupCredentials(backendFolder);

        // Then: Credential is accessible in child folder
        assertThat(backendCredentials).extracting("id").contains(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotMakeFolderCredentialAccessibleInParentFolder() throws IOException {
        // Given: A credential scoped to child folder
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var backendFolder = engineeringFolder.createProject(Folder.class, "backend");

        final var secret = createStringSecretWithFolder(SECRET, "engineering/backend");

        // When: Look up credentials from parent folder
        final var engineeringCredentials = lookupCredentials(engineeringFolder);
        final var backendCredentials = lookupCredentials(backendFolder);

        // Then: Credential is NOT accessible in parent folder, only in child
        assertThat(engineeringCredentials).extracting("id").doesNotContain(secret.name());
        assertThat(backendCredentials).extracting("id").contains(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotMakeFolderCredentialAccessibleFromRoot() throws IOException {
        // Given: A credential scoped to a folder
        final var secret = createStringSecretWithFolder(SECRET, "engineering");

        // Create folder
        jenkins.jenkins.createProject(Folder.class, "engineering");

        // When: Look up credentials from root
        final var rootCredentials = lookupCredentials(jenkins.jenkins);

        // Then: Credential is NOT accessible from root
        assertThat(rootCredentials).extracting("id").doesNotContain(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotMakeFolderCredentialAccessibleInSiblingFolder() throws IOException {
        // Given: A credential scoped to one folder
        final var secret = createStringSecretWithFolder(SECRET, "engineering");

        // Create sibling folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var qaFolder = jenkins.jenkins.createProject(Folder.class, "qa");

        // When: Look up credentials from both folders
        final var engineeringCredentials = lookupCredentials(engineeringFolder);
        final var qaCredentials = lookupCredentials(qaFolder);

        // Then: Credential is only accessible in engineering, not qa
        assertThat(engineeringCredentials).extracting("id").contains(secret.name());
        assertThat(qaCredentials).extracting("id").doesNotContain(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportMultipleFolderScopes() throws IOException {
        // Given: A credential scoped to multiple folders
        final var secret = createStringSecretWithFolder(SECRET, "engineering,qa");

        // Create folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var qaFolder = jenkins.jenkins.createProject(Folder.class, "qa");
        final var prodFolder = jenkins.jenkins.createProject(Folder.class, "prod");

        // When: Look up credentials from different folders
        final var engineeringCredentials = lookupCredentials(engineeringFolder);
        final var qaCredentials = lookupCredentials(qaFolder);
        final var prodCredentials = lookupCredentials(prodFolder);

        // Then: Credential is accessible in both engineering and qa, but not prod
        assertThat(engineeringCredentials).extracting("id").contains(secret.name());
        assertThat(qaCredentials).extracting("id").contains(secret.name());
        assertThat(prodCredentials).extracting("id").doesNotContain(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportDeepNestedFolders() throws IOException {
        // Given: A credential scoped to top-level folder
        final var secret = createStringSecretWithFolder(SECRET, "engineering");

        // Create deeply nested folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var backendFolder = engineeringFolder.createProject(Folder.class, "backend");
        final var apiFolder = backendFolder.createProject(Folder.class, "api");

        // When: Look up credentials from deeply nested folder
        final var apiCredentials = lookupCredentials(apiFolder);

        // Then: Credential is accessible in deeply nested folder
        assertThat(apiCredentials).extracting("id").contains(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldMixGlobalAndScopedCredentials() throws IOException {
        // Given: Both global and scoped credentials
        final var globalSecret = createStringSecret(SECRET);
        final var scopedSecret = createStringSecretWithFolder(SECRET, "engineering");

        // Create folders
        final var engineeringFolder = jenkins.jenkins.createProject(Folder.class, "engineering");
        final var qaFolder = jenkins.jenkins.createProject(Folder.class, "qa");

        // When: Look up credentials from different contexts
        final var engineeringCredentials = lookupCredentials(engineeringFolder);
        final var qaCredentials = lookupCredentials(qaFolder);

        // Then: Global credential accessible everywhere, scoped only in engineering
        assertThat(engineeringCredentials).extracting("id")
                .contains(globalSecret.name(), scopedSecret.name());
        assertThat(qaCredentials).extracting("id")
                .contains(globalSecret.name())
                .doesNotContain(scopedSecret.name());
    }

    @SuppressWarnings("unchecked")
    private List<StringCredentials> lookupCredentials(Folder folder) {
        // Cast to ItemGroup to resolve ambiguity between Item and ItemGroup overloads
        return (List<StringCredentials>) (List<?>) CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                (hudson.model.ItemGroup<? extends hudson.model.Item>) folder,
                ACL.SYSTEM
        );
    }

    @SuppressWarnings("unchecked")
    private List<StringCredentials> lookupCredentials(Jenkins jenkins) {
        // Cast to ItemGroup to resolve ambiguity between Item and ItemGroup overloads
        return (List<StringCredentials>) (List<?>) CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                (hudson.model.ItemGroup<? extends hudson.model.Item>) jenkins,
                ACL.SYSTEM
        );
    }

    private CreateSecretResponse createStringSecret(String secretString) {
        final var tags = List.of(AwsTags.type(Type.string));
        return createSecret(secretString, tags);
    }

    private CreateSecretResponse createStringSecretWithFolder(String secretString, String folderPath) {
        final var tags = List.of(
                AwsTags.type(Type.string),
                AwsTags.folder(folderPath)
        );
        return createSecret(secretString, tags);
    }

    private CreateSecretResponse createSecret(String secretString, List<Tag> tags) {
        return secretsManager.getClient().createSecret(secret -> {
            secret.name(CredentialNames.random());
            secret.secretString(secretString);
            secret.tags(tags);
        });
    }
}
