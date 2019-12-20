package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
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
