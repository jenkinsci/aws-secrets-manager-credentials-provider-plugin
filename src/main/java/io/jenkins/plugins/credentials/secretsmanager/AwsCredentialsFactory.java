package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;

import java.util.Map;

/**
 * Factory for RealAwsCredentials.  Determines the type of credentials to build based on the TYPE_TAG.
 */
class AwsCredentialsFactory {

    static final String TYPE_TAG = "jenkins:credentials:type";
    static final String USERNAME_PASSWORD_TYPE_VALUE = "username_password";
    static final String DEFAULT_TYPE_VALUE = "default";

    public AwsCredentials create(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        String type = tags.getOrDefault(TYPE_TAG, DEFAULT_TYPE_VALUE);

        if (USERNAME_PASSWORD_TYPE_VALUE.equals(type)) {
            // username/password
            return new RealAwsUsernameAndPasswordCredentials(id, description, tags, client);
        }

        //Default to creating older style of credentials
        return new RealAwsCredentials(id, description, tags, client);
    }
}
