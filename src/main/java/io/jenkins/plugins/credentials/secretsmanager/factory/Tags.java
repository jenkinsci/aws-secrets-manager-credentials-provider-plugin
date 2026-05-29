package io.jenkins.plugins.credentials.secretsmanager.factory;

/**
 * The tags on the Secrets Manager entry that we use.
 */
public abstract class Tags {
    private static final String namespace = "jenkins:credentials:";

    public static final String filename = namespace + "filename";
    public static final String format = namespace + "format";
    public static final String options = namespace + "options";
    public static final String type = namespace + "type";
    public static final String username = namespace + "username";

    private Tags() {

    }
}
