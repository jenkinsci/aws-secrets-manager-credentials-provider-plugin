package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import com.google.common.collect.Maps;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.git.GitSshServer;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jenkinsci.plugins.gitserver.FileBackedHttpGitRepository;
import org.jenkinsci.plugins.gitserver.HttpGitRepository;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Username With Password credentials.
 */
@RunWith(Enclosed.class)
public class StandardUsernamePasswordCredentialsIT extends AbstractPluginIT implements CredentialsTests {
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveName() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final ListBoxModel list = listCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(foo.getName(), foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAppearInCredentialsProvider() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StandardUsernamePasswordCredentials> credentials =
                lookupCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "password")
                .containsOnly(tuple(foo.getName(), "joe", Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "withCredentials([usernamePassword(credentialsId: '" + foo.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $FOO, username: $FOO_USR, password: $FOO_PSW}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****, username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });
        // And
        final StandardUsernamePasswordCredentials before = lookupCredential(StandardUsernamePasswordCredentials.class, foo.getName());

        // When
        final StandardUsernamePasswordCredentials after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "username", "password")
                .containsOnly(foo.getName(), "joe", Secret.fromString("supersecret"));
    }

    /*
     * NOTE: This is not an officially supported feature. It may change without warning in future.
     */
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAllowUsageAsStringCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    public static class GitPluginIT extends AbstractPluginIT {

        private final String username = "joe";

        @Test
        @ConfiguredWithCode(value = "/integration.yml")
        public void shouldSupportGitPlugin() throws Exception {
            final String slaveName = "agent";
            r.createSlave(Label.get(slaveName));

            // Given
            CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
                opts.tags = Maps.newHashMap();
                opts.tags.put("jenkins:credentials:username", username);
                opts.tags.put("jenkins:credentials:type", "username_password");
            });

            // When
            // Create a new repository; the path must exist
            String pipeline = Strings.m("",
                    "node('" + slaveName + "') {",
                    "  git url: 'https://github.com/jenkinsci/aws-secrets-manager-credentials-provider-plugin.git', credentialsId: '" + foo.getName() + "', branch: 'master'",
                    "}");
            final WorkflowRunResult result = runPipeline(pipeline);

            // Then
            assertSoftly(s -> {
                s.assertThat(result.log).as("Log").contains("using credential " + foo.getName());
                s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
            });
        }

    }
}
