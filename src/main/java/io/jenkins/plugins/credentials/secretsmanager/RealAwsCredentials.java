package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Map;

class RealAwsCredentials extends RealBaseAwsCredentials implements StringCredentials, StandardUsernamePasswordCredentials, SSHUserPrivateKey, StandardCertificateCredentials {

    RealAwsCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description, tags, client);
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends RealBaseAwsCredentials.DescriptorImpl {

    }
}
