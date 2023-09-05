package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.*;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class AwsSshUserPrivateKeyFactory implements AwsCredentialsFactory {
    @Override
    public String getType() {
        return Type.sshUserPrivateKey;
    }

    @Override
    public Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final var username = tags.getOrDefault(Tags.username, "");
        final var cred = new AwsSshUserPrivateKey(name, description, new StringSupplier(client, arn), username);
        return Optional.of(cred);
    }

    public static class StringSupplier extends CredentialsFactory.RealSecretsManager implements Supplier<String> {

        StringSupplier(AWSSecretsManager client, String name) {
            super(client, name);
        }

        @Override
        public String get() {
            return getSecretValue().match(new SecretValue.Matcher<String>() {
                @Override
                public String string(String str) {
                    return str;
                }

                @Override
                public String binary(byte[] bytes) {
                    return null;
                }
            });
        }
    }
}
