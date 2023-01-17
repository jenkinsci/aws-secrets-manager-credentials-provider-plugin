package io.jenkins.plugins.credentials.secretsmanager.util;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Convenience methods for using credentials.
 */
public class JenkinsCredentials {

    private final Jenkins jenkins;

    public JenkinsCredentials(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public <C extends CredentialsStore> CredentialsStore lookupStore(Class<C> type) {
        final var itStores = CredentialsProvider.lookupStores(jenkins);

        return StreamSupport.stream(itStores.spliterator(), false)
                .filter(store -> store.getClass().equals(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find the specified CredentialsStore"));
    }

    public <C extends Credentials> List<C> lookup(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, jenkins, ACL.SYSTEM, List.of());
    }

    public <C extends StandardCredentials> ListBoxModel list(Class<C> type) {
        return CredentialsProvider.listCredentials(type, jenkins, null, null, null);
    }

    public <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return lookup(type).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Could not find a credential with id <%s>", id)));
    }
}
