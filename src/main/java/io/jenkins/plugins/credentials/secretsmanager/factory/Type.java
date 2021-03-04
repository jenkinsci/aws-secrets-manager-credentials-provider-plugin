package io.jenkins.plugins.credentials.secretsmanager.factory;

/**
 * Corresponds to the Jenkinsfile credentials binding type names.
 */
public abstract class Type {
    public static final String certificate = "certificate";
    public static final String file = "file";
    public static final String usernamePassword = "usernamePassword";
    public static final String sshUserPrivateKey = "sshUserPrivateKey";
    public static final String string = "string";
    public static final String githubApp = "githubApp";

    private Type() {

    }
}
