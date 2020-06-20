package io.jenkins.plugins.credentials.secretsmanager.util;

import io.jenkins.plugins.casc.misc.EnvVarsRule;
import org.junit.rules.TestRule;

public class Rules {
    public static TestRule awsAccessKey(String id, String secret) {
        return new EnvVarsRule()
                .set("AWS_ACCESS_KEY_ID", id)
                .set("AWS_SECRET_ACCESS_KEY", secret);
    }
}
