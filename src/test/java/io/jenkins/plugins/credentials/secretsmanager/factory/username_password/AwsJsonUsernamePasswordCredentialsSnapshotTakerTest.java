package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import java.util.function.Supplier;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsSnapshotTakerTest;

public class AwsJsonUsernamePasswordCredentialsSnapshotTakerTest extends
        BaseAwsJsonCredentialsSnapshotTakerTest<AwsJsonUsernamePasswordCredentials, AwsJsonUsernamePasswordCredentialsSnapshotTaker> {

    public AwsJsonUsernamePasswordCredentialsSnapshotTakerTest() {
        super(AwsJsonUsernamePasswordCredentialsSnapshotTaker.class, AwsJsonUsernamePasswordCredentials.class);
    }

    @Override
    protected AwsJsonUsernamePasswordCredentials makeCredential() {
        final String json = AwsJsonUsernamePasswordCredentialsTest.mkUsernameAndPasswordJson("someUsername",
                "somePassword");
        final Secret secret = Secret.fromString(json);
        final Supplier<Secret> s = super.mkSupplier(secret);
        return new AwsJsonUsernamePasswordCredentials("someId", "someDescription", s);
    }
}
