package io.jenkins.plugins.credentials.secretsmanager.factory.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AwsAccessKeysCredentials extends BaseStandardCredentials implements AmazonWebServicesCredentials, AWSCredentialsProvider {

    private final Supplier<Secret> awsSecretKeySupplier;

    private final String awsAccessKeyId;
    private final String iamRoleArn;
    private final String iamExternalId;
    private final String iamMfaSerialNumber;
    private final Integer stsTokenDuration;

    public AwsAccessKeysCredentials(String id, String description, Supplier<Secret> awsSecretKeySupplier, String awsAccessKeyId, String iamRoleArn, String iamExternalId, String iamMfaSerialNumber, Integer stsTokenDuration) {
        super(id, description);
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretKeySupplier = awsSecretKeySupplier;
        this.iamRoleArn = iamRoleArn;
        this.iamExternalId = iamExternalId;
        this.iamMfaSerialNumber = iamMfaSerialNumber;
        this.stsTokenDuration = stsTokenDuration;
    }

    @Override
    public String getDisplayName() {
        return getId();
    }

    @Override
    public AWSCredentials getCredentials(String mfaToken) {
        return getCredentialsImpl().getCredentials(mfaToken);
    }

    @Override
    public AWSCredentials getCredentials() {
        return getCredentialsImpl().getCredentials();
    }

    public AWSCredentialsImpl getCredentialsImpl() {
        AWSCredentialsImpl credentials = new AWSCredentialsImpl(CredentialsScope.GLOBAL, getId(), this.awsAccessKeyId, this.awsSecretKeySupplier.get().getPlainText(), getDescription(),this.iamRoleArn, this.iamMfaSerialNumber, this.iamExternalId);
        if(stsTokenDuration != null) {
            credentials.setStsTokenDuration(this.stsTokenDuration);
        }
        return credentials;
    }

    @Override
    public void refresh() {
        throw new NotImplementedException();
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.awsAccessKeys();
        }

        @Override
        public String getIconClassName() {
            return "symbol-credentials plugin-credentials";
        }

        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return provider instanceof AwsCredentialsProvider;
        }
    }

    public Secret getAwsSecretKey() {
        return awsSecretKeySupplier.get();
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getIamRoleArn() {
        return iamRoleArn;
    }

    public String getIamExternalId() {
        return iamExternalId;
    }

    public String getIamMfaSerialNumber() {
        return iamMfaSerialNumber;
    }

    public Integer getStsTokenDuration() {
        return stsTokenDuration;
    }
}
