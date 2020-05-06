package io.jenkins.plugins.credentials.secretsmanager.util;

import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;

/**
 * Extensions on the standard jenkins rule, which should be merged to core in time.
 */
public class MyJenkinsConfiguredWithCodeRule extends JenkinsConfiguredWithCodeRule {

    public JenkinsCredentials getCredentials() {
        return new JenkinsCredentials(this.jenkins);
    }

    public Pipelines getPipelines() {
        return new Pipelines(this.jenkins);
    }
}
