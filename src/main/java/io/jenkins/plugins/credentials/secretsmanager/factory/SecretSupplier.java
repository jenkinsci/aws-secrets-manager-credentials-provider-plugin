package io.jenkins.plugins.credentials.secretsmanager.factory;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import hudson.util.Secret;

import java.util.function.Supplier;

public class SecretSupplier extends CredentialsFactory.RealSecretsManager implements Supplier<Secret> {

    public SecretSupplier(AWSSecretsManager client, String name) {
        super(client, name);
    }

    @Override
    public Secret get() {
        return getSecretValue().match(new SecretValue.Matcher<Secret>() {
            @Override
            public Secret string(String str) {
                return Secret.fromString(str);
            }

            @Override
            public Secret binary(byte[] bytes) {
                return null;
            }
        });
    }
}
