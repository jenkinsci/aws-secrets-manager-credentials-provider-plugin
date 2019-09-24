package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.IdCredentials;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation.Result;
import io.jenkins.plugins.credentials.secretsmanager.util.DeleteSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.RestoreSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.TestUtils;

public abstract class AbstractPluginIT {

    private static final String BAR = "bar";
    private static final String FOO = "foo";
    private static final AWSSecretsManager CLIENT = TestUtils.getClientSecretsManager();

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private CredentialsStore store;

    @BeforeClass
    public static void fakeAwsCredentials() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretKey", "test");
    }

    @Before
    public void setup() {
        store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();

        for (String secretId: Arrays.asList(FOO, BAR)) {
            restoreSecret(secretId);
            deleteSecret(secretId, opts -> opts.force = true);
        }
    }

    WorkflowRunResult runPipeline(String... definition) {
        final String def = String.join("\n", definition);
        return this.runPipeline(def);
    }

    WorkflowRunResult runPipeline(String definition) {
        try {
            final WorkflowJob project = r.jenkins.createProject(WorkflowJob.class, "example");
            project.setDefinition(new CpsFlowDefinition(definition, true));
            final QueueTaskFuture<WorkflowRun> workflowRunFuture = project.scheduleBuild2(0);
            final WorkflowRun workflowRun = workflowRunFuture.waitForStart();
            r.waitForCompletion(workflowRun);

            final String log = workflowRun.getLog();
            final hudson.model.Result result = workflowRun.getResult();
            return new WorkflowRunResult(log, result);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static class WorkflowRunResult {
        final String log;
        final hudson.model.Result result;

        private WorkflowRunResult(String log, hudson.model.Result result) {
            this.log = log;
            this.result = result;
        }
    }

    <C extends Credentials> List<C> lookupCredentials(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, r.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    <C extends IdCredentials> List<String> lookupCredentialNames(Class<C> type) {
        final ListBoxModel result = CredentialsProvider.listCredentials(type, r.jenkins, ACL.SYSTEM, null, null);

        return result.stream()
            .map(o -> o.name)
            .collect(Collectors.toList());
    }

    Result createSecret(String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        // FIXME use a unique name
        return create.run(FOO, secretString);
    }

    Result createOtherSecret(String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        // FIXME use a unique name
        return create.run(BAR, secretString);
    }

    Result createSecret(byte[] secretBinary) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        // FIXME use a unique name
        return create.run(FOO, secretBinary);
    }

    Result createSecret(String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        // FIXME use a unique name
        return create.run(FOO, secretString, opts);
    }

    Result createOtherSecret(String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        // FIXME use a unique name
        return create.run(BAR, secretString, opts);
    }

    void deleteSecret(String secretId) {
        final DeleteSecretOperation delete = new DeleteSecretOperation(CLIENT);
        delete.run(secretId);
    }

    CredentialsStore store() {
        return this.store;
    }

    private void deleteSecret(String secretId, Consumer<DeleteSecretOperation.Opts> opts) {
        final DeleteSecretOperation delete = new DeleteSecretOperation(CLIENT);
        delete.run(secretId, opts);
    }

    private static void restoreSecret(String secretId) {
        final RestoreSecretOperation restore = new RestoreSecretOperation(CLIENT);
        restore.run(secretId);
    }
}
