package io.jenkins.plugins.credentials.secretsmanager.util;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsCredentials {

    private final Jenkins jenkins;

    public JenkinsCredentials(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public <C extends Credentials> List<C> lookup(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    public <C extends IdCredentials> ListBoxModel list(Class<C> type) {
        return CredentialsProvider.listCredentials(type, jenkins, null, null, null);
    }

    public <C extends IdCredentials> List<String> lookupCredentialNames(Class<C> type) {
        final ListBoxModel result = CredentialsProvider.listCredentials(type, jenkins, ACL.SYSTEM, null, null);

        return result.stream()
                .map(o -> o.name)
                .collect(Collectors.toList());
    }

    public <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return lookup(type).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected a credential but none was present"));
    }
}
