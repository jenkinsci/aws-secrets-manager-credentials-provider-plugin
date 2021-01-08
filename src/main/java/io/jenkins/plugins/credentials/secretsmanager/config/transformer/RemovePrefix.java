package io.jenkins.plugins.credentials.secretsmanager.config.transformer;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RemovePrefix extends Transformer {

    private String prefix;

    @DataBoundConstructor
    public RemovePrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    @DataBoundSetter
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String transform(String str) {
        return theTransform(str, prefix);
    }

    @Override
    public String inverse(String str) {
        return fixNullAndTrim(prefix).concat(str);
    }

    private static String theTransform(String str, String prefix) {
        final String canonicalPrefix = fixNullAndTrim(prefix);

        if (str.startsWith(canonicalPrefix)) {
            return Pattern.compile(canonicalPrefix, Pattern.LITERAL)
                    .matcher(str)
                    .replaceFirst("");
        } else {
            return str;
        }
    }

    /**
     * Convert null to empty string, and trim whitespace.
     */
    private static String fixNullAndTrim(String s) {
        return Util.fixNull(s).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemovePrefix that = (RemovePrefix) o;
        return Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix);
    }

    @Extension
    @Symbol("removePrefix")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Transformer.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.removePrefix();
        }

        public FormValidation doCheckPrefix(@QueryParameter String prefix) {
            if (Util.fixEmptyAndTrim(prefix) == null) {
                return FormValidation.warning("Prefix should not be empty");
            }
            return FormValidation.ok();
        }

        @POST
        @SuppressWarnings("unused")
        public FormValidation doTestTransformation(
                @QueryParameter("prefix") final String prefix) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            final PluginConfiguration pluginConfiguration = PluginConfiguration.getInstance();

            final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClient.builder();

            Optional.ofNullable(pluginConfiguration.getEndpointConfiguration())
                    .map(EndpointConfiguration::build)
                    .ifPresent(builder::withEndpointConfiguration);

            final AWSSecretsManager client = builder.build();

            final ListSecretsResult result = client.listSecrets(new ListSecretsRequest());

            final Map<String, Long> transformedIds = result.getSecretList().stream()
                    .map(SecretListEntry::getName)
                    .map(name -> theTransform(name, prefix))
                    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

            final boolean transformedIdsAreNotUnique = transformedIds.values().stream()
                    .anyMatch(count -> count > 1);

            if (transformedIdsAreNotUnique) {
                final StringBuilder sb = new StringBuilder();
                sb.append(Messages.transformationProducedCredentialIdsThatWereNotUnique()).append(":\n");
                transformedIds
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() > 1)
                        .forEach(entry ->
                                sb.append(Messages.duplicateCredentialId(entry.getKey(), entry.getValue())).append("\n"));
                return FormValidation.error(sb.toString());
            } else {
                return FormValidation.ok(Messages.success());
            }
        }
    }
}
