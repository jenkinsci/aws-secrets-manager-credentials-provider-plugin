package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import org.jenkinsci.plugins.pipeline.modeldefinition.credentials.impl.SSHUserPrivateKeyHandler;
import org.jenkinsci.plugins.pipeline.modeldefinition.credentials.impl.StringCredentialsHandler;
import org.jenkinsci.plugins.pipeline.modeldefinition.credentials.impl.UsernamePasswordHandler;
import org.jenkinsci.plugins.pipeline.modeldefinition.model.CredentialsBindingHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.security.ACL;
import jenkins.model.Jenkins;

/**
 * Patch the credentials binding behavior of AwsCredentials so it works like the standard
 * single-type binders from pipeline-model-definition plugin.
 *
 * This handler is only activated if the pipeline-model-definition plugin is installed.
 */
@Extension(optional = true)
@SuppressWarnings("unused")
public class AwsCredentialsHandler extends CredentialsBindingHandler<AwsCredentials> {

    @Nonnull
    @Override
    public Class<? extends StandardCredentials> type() {
        return AwsCredentials.class;
    }

    @Nonnull
    @Override
    public List<Map<String, Object>> getWithCredentialsParameters(String credentialsId) {
        return detectType(credentialsId).match(new CredentialsType.Matcher<List<Map<String, Object>>>() {

            @Override
            public List<Map<String, Object>> string() {
                return new StringCredentialsHandler().getWithCredentialsParameters(credentialsId);
            }

            @Override
            public List<Map<String, Object>> sshUserPrivateKey() {
                return new SSHUserPrivateKeyHandler().getWithCredentialsParameters(credentialsId);
            }

            @Override
            public List<Map<String, Object>> usernamePassword() {
                return new UsernamePasswordHandler().getWithCredentialsParameters(credentialsId);
            }

            @Override
            public List<Map<String, Object>> none() {
                return Collections.emptyList();
            }
        });
    }

    private static CredentialsType detectType(String credentialsId) {
        final AwsCredentials credential =
                CredentialsProvider.lookupCredentials(AwsCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList())
                .stream()
                .filter(c -> c.getId().equals(credentialsId))
                .findFirst()
                .orElseThrow(() -> new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(credentialsId)));

        final GetSecretValueResult result = credential.getSecretValue();
        final Map<String, String> tags = credential.getTags();

        if (result.getSecretString() != null) {
            if (tags.containsKey(AwsCredentials.USERNAME_TAG)) {
                if (SSHKeyValidator.isValid(result.getSecretString())) {
                    return CredentialsType.sshUserPrivateKey();
                } else {
                    return CredentialsType.usernamePassword();
                }
            }

            return CredentialsType.string();
        }

        return CredentialsType.none();
    }

    /**
     * An Algebraic Data Type to transform a multi-type Jenkins Credential into its most appropriate
     * single-type representation.
     */
    private abstract static class CredentialsType {

        static CredentialsType string() {
            return new StringHolder();
        }

        static CredentialsType usernamePassword() {
            return new UsernamePasswordHolder();
        }

        static CredentialsType sshUserPrivateKey() {
            return new SshUserPrivateKeyHolder();
        }

        // When the AWS secret value did not match any supported type
        static CredentialsType none() {
            return new None();
        }

        abstract <R> R match(Matcher<R> matcher);

        interface Matcher<R> {
            R none();

            R string();

            R sshUserPrivateKey();

            R usernamePassword();
        }

        private static class StringHolder extends CredentialsType {

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.string();
            }
        }

        private static class None extends CredentialsType {

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.none();
            }
        }

        private static class UsernamePasswordHolder extends CredentialsType {

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.usernamePassword();
            }
        }

        private static class SshUserPrivateKeyHolder extends CredentialsType {

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.sshUserPrivateKey();
            }
        }
    }
}
