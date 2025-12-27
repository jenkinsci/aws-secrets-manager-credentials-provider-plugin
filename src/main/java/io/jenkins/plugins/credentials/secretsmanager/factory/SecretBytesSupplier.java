package io.jenkins.plugins.credentials.secretsmanager.factory;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.SecretBytes;

import java.util.function.Supplier;

public class SecretBytesSupplier extends CredentialsFactory.RealSecretsManager implements Supplier<SecretBytes> {

    public SecretBytesSupplier(AWSSecretsManager client, String name) {
        super(client, name);
    }

    @Override
    public SecretBytes get() {
        return getSecretValue().match(new SecretValue.Matcher<SecretBytes>() {
            @Override
            public SecretBytes string(String str) {
                return null;
            }

            @Override
            public SecretBytes binary(byte[] bytes) {
                return SecretBytes.fromBytes(bytes);
            }
        });
    }
}
