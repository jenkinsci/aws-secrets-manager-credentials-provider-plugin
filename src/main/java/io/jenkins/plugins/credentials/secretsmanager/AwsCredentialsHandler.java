package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import org.jenkinsci.plugins.pipeline.modeldefinition.model.CredentialsBindingHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import hudson.Extension;

/**
 * Patch the credentials binding behavior of AwsCredentials so it works like the standard
 * single-type binders from pipeline-model-definition plugin.
 */
// FIXME make this an OptionalExtension (and make pipeline-model-definition an optional dependency)
@Extension
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
        Map<String, Object> map = new HashMap<>();
        map.put("$class", AwsCredentialsBinding.class.getName());
        map.put("credentialsId", credentialsId);
        map.put("variable", new EnvVarResolver("%s"));
        map.put("usernameVariable", new EnvVarResolver("%s_USR"));
        map.put("passwordVariable", new EnvVarResolver("%s_PSW"));
        map.put("passphraseVariable", new EnvVarResolver("%s_PSW"));
        return Collections.singletonList(map);
    }
}
