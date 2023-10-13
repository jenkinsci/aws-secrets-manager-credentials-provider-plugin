package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.jvnet.hudson.test.FlagRule;

import java.util.ArrayList;
import java.util.Optional;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;


/**
 * The plugin should support AmazonWebServicesCredentials.
 */
public class AWSAccessKeysCredentialsIT implements CredentialsTests {

    private static final String ACCESSKEYID = "somekeyid";
    private static final String SECRETKEY = "supersecretkey";
    private static final String IAMROLEARN = "somearn";
    private static final String IAMEXTERNALID = "someexternalid";
    private static final String MFASERIALNUMBER = "somemfaserialnumber";
    private static final Integer STSTOKENDURATION = 77777;

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);

        // When
        final var credentialList = jenkins.getCredentials().list(AmazonWebServicesCredentials.class);
        // Then
        assertThat(credentialList)
                .containsOption(secret.getName(), secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveSecretKey() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasSecretKey(SECRETKEY);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveIamExternalId() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY, Optional.of(AwsTags.iamexternalid(IAMEXTERNALID)));

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasIamExternalId(IAMEXTERNALID);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveIamRoleArn() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY, Optional.of(AwsTags.iamrolearn(IAMROLEARN)));

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasIamRoleArn(IAMROLEARN);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveIamMfaSerialNumberId() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY, Optional.of(AwsTags.iammfaserialnumberid(MFASERIALNUMBER)));

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasIamMfaSerialNumber(MFASERIALNUMBER);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveStsTokenDuration() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY, Optional.of(AwsTags.ststokenduration(STSTOKENDURATION.toString())));

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasStsTokenDuration(STSTOKENDURATION);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveAccessKeyId() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasAccessKeyId(ACCESSKEYID);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);

        // When
        final var credential =
                jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasId(secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);

        // When
        final var run = runPipeline("",
                "withCredentials([aws(credentialsId: '" + secret.getName() + "')]) {",
                "  echo \"Credential: {accesskeyid: $AWS_ACCESS_KEY_ID, secretkey: $AWS_SECRET_ACCESS_KEY}\"",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {accesskeyid: ****, secretkey: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);
        // Workaround for test failures due to this https://github.com/jenkinsci/aws-credentials-plugin/issues/68
        // Without setting this flag to false, the test would fail with an illegal argument exception.
        // See https://github.com/jenkinsci/structs-plugin/blob/master/src/main/java/org/jenkinsci/plugins/structs/describable/DescribableModel.java#L321
        // However in a live jenkins instance the flag is set to false so a warning
        // is logged and the build happily proceeds. Didn't dig any farther into why this field gets set to true in
        // the test harness but this will work until that issue in the aws-credentials-plugin is fixed.
        DescribableModel.STRICT_PARAMETER_CHECKING = false;
        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + secret.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{accessKey: $AWS_ACCESS_KEY_ID, secretKey: $AWS_SECRET_ACCESS_KEY}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasLogContaining("{accessKey: ****, secretKey: ****}")
                .hasResult(hudson.model.Result.SUCCESS);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretResult foo = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);
        final AmazonWebServicesCredentials before = jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, foo.getName());

        // When
        final AmazonWebServicesCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertThat(after)
                .hasSecretKey(before.getCredentials().getAWSSecretKey())
                .hasAccessKeyId(before.getCredentials().getAWSAccessKeyId())
                .hasId(before.getId());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final var secret = createAwsAccessKeysSecret(ACCESSKEYID, SECRETKEY);

        final var ours = jenkins.getCredentials().lookup(AmazonWebServicesCredentials.class, secret.getName());

        final var theirs = new AWSCredentialsImpl(null, "id", "accesskey", "secretkey", "description");

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    private CreateSecretResult createAwsAccessKeysSecret(String accessKeyId, String secretKey) {
        return createAwsAccessKeysSecret(accessKeyId, secretKey, Optional.empty());
    }
    private CreateSecretResult createAwsAccessKeysSecret(String accessKeyId, String secretKey, Optional<Tag> extraTag) {

        final var tags = new ArrayList();
        tags.add(AwsTags.type(Type.awsAccessKeys));
        tags.add(AwsTags.accesskeyid(accessKeyId));
        if(extraTag.isPresent()) {
            tags.add(extraTag.get());
        }

        final var request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretKey)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }

    @ClassRule
    public static TestRule noStrictParameterChecking = FlagRule.systemProperty("org.jenkinsci.plugins.structs.describable.DescribableModel.STRICT_PARAMETER_CHECKING", "false");
}

