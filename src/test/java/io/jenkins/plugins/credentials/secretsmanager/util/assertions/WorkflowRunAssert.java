package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.model.Result;

import java.io.IOException;
import java.util.Objects;

public class WorkflowRunAssert extends AbstractAssert<WorkflowRunAssert, WorkflowRun> {
    public WorkflowRunAssert(WorkflowRun actual) {
        super(actual, WorkflowRunAssert.class);
    }

    public WorkflowRunAssert hasResult(Result result) {
        isNotNull();

        if (!Objects.equals(actual.getResult(), result)) {
            failWithMessage("Expected workflow's result to be <%s> but was <%s>", result, actual.getResult());
        }

        return this;
    }

    public WorkflowRunAssert hasLogContaining(String str) {
        isNotNull();

        String actualLog = "";
        try {
            actualLog = actual.getLog();
        } catch (IOException e) {
            failWithMessage("Could not read workflow's log");
        }

        if (!actualLog.contains(str)) {
            failWithMessage("Expected workflow's log to contain <%s> but it did not", str);
        }

        return this;
    }
}
