package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import org.apache.commons.lang3.SerializationUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;

public abstract class AbstractPluginIT {

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Rule
    public AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @BeforeClass
    public static void fakeAwsCredentials() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretKey", "test");
    }

    WorkflowRun runPipeline(String definition) {
        try {
            final WorkflowJob project = r.jenkins.createProject(WorkflowJob.class, "example");
            project.setDefinition(new CpsFlowDefinition(definition, true));
            final QueueTaskFuture<WorkflowRun> workflowRunFuture = project.scheduleBuild2(0);
            final WorkflowRun workflowRun = workflowRunFuture.waitForStart();
            r.waitForCompletion(workflowRun);

            return workflowRun;
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    <C extends Credentials> List<C> lookupCredentials(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, r.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    <C extends IdCredentials> ListBoxModel listCredentials(Class<C> type) {
        return CredentialsProvider.listCredentials(type, r.jenkins, null, null, null);
    }

    <C extends IdCredentials> List<String> lookupCredentialNames(Class<C> type) {
        final ListBoxModel result = CredentialsProvider.listCredentials(type, r.jenkins, ACL.SYSTEM, null, null);

        return result.stream()
            .map(o -> o.name)
            .collect(Collectors.toList());
    }

    <C extends StandardCredentials> C lookupCredential(Class<C> type, String id) {
        return lookupCredentials(type).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected a credential but none was present"));
    }

    static <C extends StandardCredentials> C snapshot(C credentials) {
        return SerializationUtils.deserialize(SerializationUtils.serialize(CredentialsProvider.snapshot(credentials)));
    }
}
