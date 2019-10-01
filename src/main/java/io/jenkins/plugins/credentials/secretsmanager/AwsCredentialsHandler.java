package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import org.jenkinsci.plugins.pipeline.modeldefinition.credentials.impl.StringCredentialsHandler;
import org.jenkinsci.plugins.pipeline.modeldefinition.model.CredentialsBindingHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;

/**
 * Patch the credentials binding behavior of AwsCredentials so it works like the standard
 * single-type binders from pipeline-model-definition plugin.
 *
 * This handler is only activated if the pipeline-model-definition plugin is installed.
 */
@Extension(optional = true)
@SuppressWarnings("unused")
public class AwsCredentialsHandler extends CredentialsBindingHandler<AwsCredentials> {

    private transient final AWSSecretsManager client = PluginConfiguration.getInstance().getClient();

    @Nonnull
    @Override
    public Class<? extends StandardCredentials> type() {
        return AwsCredentials.class;
    }

    @Nonnull
    @Override
    public List<Map<String, Object>> getWithCredentialsParameters(String credentialsId) {
        // TODO get the secret's tags, to add support for Username With Password and SSH Private Key credentials
        final GetSecretValueResult result = getSecretValue(credentialsId);
        final CredentialsType credentialsType = detect(result);

        return credentialsType.match(new CredentialsType.Matcher<List<Map<String, Object>>>() {

            @Override
            public List<Map<String, Object>> string() {
                return new StringCredentialsHandler().getWithCredentialsParameters(credentialsId);
            }

            @Override
            public List<Map<String, Object>> none() {
                return Collections.emptyList();
            }
        });
    }

    private GetSecretValueResult getSecretValue(String secretName) {
        final GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);

        try {
            return client.getSecretValue(request);
        } catch (AmazonClientException ex) {
            throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(secretName));
        }
    }

    private static CredentialsType detect(GetSecretValueResult result) {

        if ((result.getSecretString() != null) && !SSHKeyValidator.isValid(result.getSecretString())) {
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

        // When the AWS secret value did not match any supported type
        static CredentialsType none() {
            return new None();
        }

        abstract <R> R match(Matcher<R> matcher);

        interface Matcher<R> {
            R none();

            R string();
        }

        private static class StringHolder extends CredentialsType {

            private StringHolder() {}

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.string();
            }
        }

        private static class None extends CredentialsType {

            private None() {}

            @Override
            <R> R match(Matcher<R> matcher) {
                return matcher.none();
            }
        }
    }
}
