package io.jenkins.plugins.credentials.secretsmanager.util;

import java.util.UUID;

public class CredentialNames {
    /**
     * @return a random name for a Jenkins credential
     */
    public static String random() {
        // The CredentialsNameProvider does not like hyphens in the name, so we remove them
        return UUID.randomUUID().toString().replace("-", "");
    }
}
