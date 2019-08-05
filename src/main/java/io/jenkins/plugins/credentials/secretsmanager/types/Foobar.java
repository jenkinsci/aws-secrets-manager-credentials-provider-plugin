package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class Foobar {

    private Foobar() {

    }

    static StandardCredentials fromSecret(GetSecretValueResult s) {
        final String name = s.getName();
        final List<Tag> tags = s.getSdkHttpMetadata().;
        final IdCredentials cred = new AwsStringCredentials(name, description, client);
    }

    public interface Matcher {
        Optional<StandardCredentials> match(String secretValue, Map<String, String> tags);
    }

    private static class MatcherChain implements Matcher {

        private final List<Matcher> chain;

        private MatcherChain(List<Matcher> chain) {
            this.chain = chain;
        }

        @Override
        public Optional<StandardCredentials> match(String secretValue, Map<String, String> tags) {

            return chain.stream()
                    .map(matcher -> matcher.match(secretValue, ))
                    .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                    .findFirst();
        }
    }

    private static class SshCredentialMatcher implements Matcher {

        private final transient AWSSecretsManager client;

        private SshCredentialMatcher(AWSSecretsManager client) {
            this.client = client;
        }

        public Optional<StandardCredentials> match(String secretValue, Map<String, String> tags) {
            if (hasAsciiArmor(secretValue) && hasUsername(tags)) {
                return new AwsSshCredentials(client);

                new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource()
            }

            return Optional.empty();
        }

        private static boolean hasAsciiArmor(String str) {
            return str.matches("^-{2,}\\s*BEGIN .+ PRIVATE KEY\\s*-{2,}")
                    && str.matches("^-{2,}\\s*END .+ PRIVATE KEY\\s*-{2,}");
        }

        private static boolean hasUsername(Map<String, String> tags) {
            return tags.containsKey("username");
        }
    }

    private static class UsernamePasswordCredentialMatcher implements Matcher {

        private final transient AWSSecretsManager client;

        private UsernamePasswordCredentialMatcher(AWSSecretsManager client) {
            this.client = client;
        }

        public Optional<StandardCredentials> match(String secretValue, Map<String, String> tags) {
            if (hasUsername(tags)) {
                return new AwsUsernamePasswordCredentials();
            }

            return Optional.empty();
        }

        private static boolean hasUsername(Map<String, String> tags) {
            return tags.containsKey("username");
        }
    }

}