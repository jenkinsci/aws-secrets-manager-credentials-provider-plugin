package io.jenkins.plugins.credentials.secretsmanager.factory;

/**
 * Corresponds to the Jenkinsfile credentials binding type names.
 */
public enum CredentialType {
    string, sshUserPrivateKey, usernamePassword, certificate, unknown;

    // TODO Create a conformance test that will fail if the credentials binding type names ever change
    public static CredentialType fromString(String type) {
        switch (type) {
            case "string":
                return string;
            case "usernamePassword":
                return usernamePassword;
            case "sshUserPrivateKey":
                return sshUserPrivateKey;
            case "certificate":
                return certificate;
            default:
                return unknown;
        }
    }
}
