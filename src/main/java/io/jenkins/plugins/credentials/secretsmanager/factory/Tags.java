package io.jenkins.plugins.credentials.secretsmanager.factory;

/**
 * The tags on the Secrets Manager entry that we use.
 */
public abstract class Tags {
    public static final String type = "jenkins:credentials:type";
    public static final String username = "jenkins:credentials:username";

    private Tags() {

    }
}
