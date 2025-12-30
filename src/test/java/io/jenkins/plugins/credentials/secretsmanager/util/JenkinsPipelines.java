package io.jenkins.plugins.credentials.secretsmanager.util;

import hudson.model.Descriptor;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import hudson.model.queue.QueueTaskFuture;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Convenience methods for using pipelines.
 */
public class JenkinsPipelines {

    private final Jenkins jenkins;

    public JenkinsPipelines(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public WorkflowRun run(String definition) {
        try {
            final WorkflowJob project = jenkins.createProject(WorkflowJob.class, "example");
            project.setDefinition(new CpsFlowDefinition(definition, true));
            final QueueTaskFuture<WorkflowRun> workflowRunFuture = project.scheduleBuild2(0);
            final WorkflowRun workflowRun = workflowRunFuture.waitForStart();
            waitForCompletion(workflowRun);
            return workflowRun;
        } catch (IOException | InterruptedException | ExecutionException | Descriptor.FormException e) {
            throw new RuntimeException(e);
        }
    }

    private <R extends Run<?, ?>> void waitForCompletion(R r) throws InterruptedException {
        while(r.isBuilding()) {
            Thread.sleep(100L);
        }
    }
}
