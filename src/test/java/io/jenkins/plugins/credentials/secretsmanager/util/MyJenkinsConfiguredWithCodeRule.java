package io.jenkins.plugins.credentials.secretsmanager.util;

import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;

/**
 * Extensions on the standard jenkins rule.
 */
public class MyJenkinsConfiguredWithCodeRule extends JenkinsConfiguredWithCodeRule {

    public JenkinsCredentials getCredentials() {
        return new JenkinsCredentials(this.jenkins);
    }

    public JenkinsPipelines getPipelines() {
        return new JenkinsPipelines(this.jenkins);
    }
}
