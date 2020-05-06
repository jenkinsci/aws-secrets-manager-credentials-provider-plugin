package io.jenkins.plugins.credentials.secretsmanager.util;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import org.apache.commons.lang3.SerializationUtils;

public final class CredentialSnapshots {
    public static <C extends StandardCredentials> C snapshot(C credentials) {
        return SerializationUtils.deserialize(SerializationUtils.serialize(CredentialsProvider.snapshot(credentials)));
    }
}
