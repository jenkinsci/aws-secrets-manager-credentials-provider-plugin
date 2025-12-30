package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import java.util.function.Supplier;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsSnapshotTakerTest;

public class AwsJsonSshUserPrivateKeySnapshotTakerTest extends
        BaseAwsJsonCredentialsSnapshotTakerTest<AwsJsonSshUserPrivateKey, AwsJsonSshUserPrivateKeySnapshotTaker> {

    public AwsJsonSshUserPrivateKeySnapshotTakerTest() {
        super(AwsJsonSshUserPrivateKeySnapshotTaker.class, AwsJsonSshUserPrivateKey.class);
    }

    @Override
    protected AwsJsonSshUserPrivateKey makeCredential() {
        final String json = AwsJsonSshUserPrivateKeyTest.mkUsernameKeyAndPassphraseJson("someUsername", "someKey",
                "somePassphrase");
        final Secret secret = Secret.fromString(json);
        final Supplier<Secret> s = super.mkSupplier(secret);
        return new AwsJsonSshUserPrivateKey("someId", "someDescription", s);
    }
}
