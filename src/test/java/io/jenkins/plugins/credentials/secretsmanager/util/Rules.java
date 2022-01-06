package io.jenkins.plugins.credentials.secretsmanager.util;

import io.jenkins.plugins.casc.misc.EnvVarsRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.jvnet.hudson.test.JenkinsRule;

public class Rules {
    public static TestRule awsAccessKey(String id, String secret) {
        return new EnvVarsRule()
                .set("AWS_ACCESS_KEY_ID", id)
                .set("AWS_SECRET_ACCESS_KEY", secret);
    }

    public static TestRule jenkinsWithSecretsManager(JenkinsRule jenkins, AWSSecretsManagerRule secretsManager) {
        return RuleChain
                .outerRule(secretsManager)
                .around(new DeferredEnvironmentVariables()
                        .set("AWS_ACCESS_KEY_ID", "fake")
                        .set("AWS_SECRET_ACCESS_KEY", "fake")
                        .set("AWS_SERVICE_ENDPOINT", secretsManager::getServiceEndpoint)
                        .set("AWS_SIGNING_REGION", secretsManager::getSigningRegion))
                .around(jenkins);
    }
}
