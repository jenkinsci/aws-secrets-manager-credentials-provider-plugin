package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.*;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.*;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(JMockit.class)
public class GithubAppCredentialsIT implements CredentialsTests {

    private static final String APP_ID = "11111";
    private static final String PRIVATE_KEY = Crypto.newPrivateKey()
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "");;

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @BeforeClass
    public static void checkClassExists() {
        Optional<Class> clazz = getGithubAppCredentialClass();
        assumeTrue(clazz.isPresent());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);

        // When
        final ListBoxModel list = jenkins.getCredentials().list(StandardCredentials.class);

        // Then
        assertThat(list)
                .containsOption(APP_ID + "/******", foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);
        final GitHubAppCredentials ours = lookup(GitHubAppCredentials.class, foo.getName());

        final GitHubAppCredentials theirs = new GitHubAppCredentials(CredentialsScope.GLOBAL, "name", "description", "11111", Secret.fromString("secret"));

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);

        new MockUp<GitHubAppCredentials>() {
            @Mock
            public Secret getPassword() {
                return Secret.fromString("test-token");
            }
        };

        // When
        final WorkflowRun run = runPipeline("",
                "withCredentials([usernamePassword(credentialsId: '" + foo.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding(){
        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);

        new MockUp<GitHubAppCredentials>() {
            @Mock
            public Secret getPassword() {
                return Secret.fromString("test-token");
            }
        };

        // When
        final WorkflowRun run = runPipeline("",
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
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****, username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {

        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);
        final GitHubAppCredentials before = jenkins.getCredentials().lookup(GitHubAppCredentials.class, foo.getName());

        // When
        final GitHubAppCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertThat(after)
                .hasUsername(before.getUsername())
                .hasPrivateKey(before.getPrivateKey())
                .hasId(before.getId());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);

        // When
        final GitHubAppCredentials credential =
                jenkins.getCredentials().lookup(GitHubAppCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasId(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsername() {
        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);

        // When
        final StandardUsernamePasswordCredentials credential =
                jenkins.getCredentials().lookup(GitHubAppCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasUsername(APP_ID);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePrivateKey() {
        // Given
        final CreateSecretResult foo = createGitHubAppCredentialSecret(APP_ID, PRIVATE_KEY);

        // When
        final GitHubAppCredentials credential = lookup(GitHubAppCredentials.class, foo.getName());

        // Then
        assertThat(credential)
                .hasPrivateKey(PRIVATE_KEY);
    }

    private CreateSecretResult createGitHubAppCredentialSecret(String appId, String privateKey) {
        final List<Tag> tags = Lists.of(
                AwsTags.type(Type.githubApp),
                AwsTags.appid(appId));

        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(privateKey)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }

    private <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return jenkins.getCredentials().lookup(type, id);
    }

    private static Optional<Class> getGithubAppCredentialClass() {
        Class githubCredentials;
        try {
            githubCredentials = Class.forName("org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials");
        } catch (Throwable ex) {
            return Optional.empty();
        }
        return Optional.of(githubCredentials);
    }
}
