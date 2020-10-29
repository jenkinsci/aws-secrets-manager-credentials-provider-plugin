package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.security.KeyStore;
import java.util.Optional;

public class CustomAssertions {

    public static <T> OptionalAssert<T> assertThat(Optional<T> actual) {
        return new OptionalAssert<>(actual);
    }

    public static StandardCredentialsAssert assertThat(StandardCredentials actual) {
        return new StandardCredentialsAssert(actual);
    }

    public static FileCredentialsAssert assertThat(FileCredentials actual) {
        return new FileCredentialsAssert(actual);
    }

    public static StringCredentialsAssert assertThat(StringCredentials actual) {
        return new StringCredentialsAssert(actual);
    }

    public static StandardUsernamePasswordCredentialsAssert assertThat(StandardUsernamePasswordCredentials actual) {
        return new StandardUsernamePasswordCredentialsAssert(actual);
    }

    public static StandardCertificateCredentialsAssert assertThat(StandardCertificateCredentials actual) {
        return new StandardCertificateCredentialsAssert(actual);
    }

    public static SSHUserPrivateKeyAssert assertThat(SSHUserPrivateKey actual) {
        return new SSHUserPrivateKeyAssert(actual);
    }

    public static WorkflowRunAssert assertThat(WorkflowRun actual) {
        return new WorkflowRunAssert(actual);
    }

    public static ListBoxModelAssert assertThat(ListBoxModel actual) {
        return new ListBoxModelAssert(actual);
    }

    public static KeyStoreAssert assertThat(KeyStore actual) {
        return new KeyStoreAssert(actual);
    }
}
