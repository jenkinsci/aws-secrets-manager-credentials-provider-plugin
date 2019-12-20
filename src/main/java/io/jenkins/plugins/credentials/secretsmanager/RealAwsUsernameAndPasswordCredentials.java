package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;

import java.util.Map;

class RealAwsUsernameAndPasswordCredentials extends RealBaseAwsCredentials implements StandardUsernamePasswordCredentials {

    RealAwsUsernameAndPasswordCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description, tags, client);
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends RealBaseAwsCredentials.DescriptorImpl {

    }
}
