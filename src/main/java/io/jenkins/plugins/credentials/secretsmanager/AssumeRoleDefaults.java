package io.jenkins.plugins.credentials.secretsmanager;

public abstract class AssumeRoleDefaults {

    public static final int SESSION_DURATION_SECONDS = 900;
    public static final String SESSION_NAME = "io.jenkins.plugins.aws-secrets-manager-credentials-provider";

    private AssumeRoleDefaults() {

    }
}
