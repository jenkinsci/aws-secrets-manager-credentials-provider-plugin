package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.aws.AwsAccessKeysCredentials;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class AmazonWebServicesCredentialsAssert extends AbstractAssert<AmazonWebServicesCredentialsAssert, AmazonWebServicesCredentials> {

    public AmazonWebServicesCredentialsAssert(AmazonWebServicesCredentials actual) {
        super(actual, AmazonWebServicesCredentialsAssert.class);
    }

    public AmazonWebServicesCredentialsAssert hasAccessKeyId(String accessKeyId) {
        isNotNull();

        String actualAccessKeyId = actual.getCredentials().getAWSAccessKeyId();
        if (!Objects.equals(actualAccessKeyId, accessKeyId)) {
            failWithMessage("Expected accessKeyId to be <%s> but was <%s>", accessKeyId, actualAccessKeyId);
        }

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasSecretKey(String secretKey) {
        isNotNull();
        String actualSecretKey = actual.getCredentials().getAWSSecretKey();
        if (!Objects.equals(actualSecretKey, secretKey)) {
            failWithMessage("Expected secretkey to be <%s> but was <%s>", secretKey, actualSecretKey);
        }

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasIamRoleArn(String iamRoleArn) {
        isNotNull();
        String actualIamRoleArn = ((AwsAccessKeysCredentials)actual).getIamRoleArn();
        if (!Objects.equals(actualIamRoleArn, iamRoleArn)) {
            failWithMessage("Expected iamRoleArn to be <%s> but was <%s>", iamRoleArn, actualIamRoleArn);
        }

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasIamExternalId(String iamExternalId) {
        isNotNull();
        String actualIamExternalId = ((AwsAccessKeysCredentials)actual).getIamExternalId();
        if (!Objects.equals(actualIamExternalId, iamExternalId)) {
            failWithMessage("Expected iamExternalId to be <%s> but was <%s>", iamExternalId, actualIamExternalId);
        }

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasIamMfaSerialNumber(String iamMfaSerialNumber) {
        isNotNull();
        String actualIamMfaSerialNumber = ((AwsAccessKeysCredentials)actual).getIamMfaSerialNumber();
        if (!Objects.equals(actualIamMfaSerialNumber, iamMfaSerialNumber)) {
            failWithMessage("Expected iamMfaSerialNumber to be <%s> but was <%s>", iamMfaSerialNumber, actualIamMfaSerialNumber);
        }

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasStsTokenDuration(Integer stsTokenDuration) {
        isNotNull();
        Integer actualStsTokenDuration = ((AwsAccessKeysCredentials)actual).getStsTokenDuration();
        if (!Objects.equals(actualStsTokenDuration, stsTokenDuration)) {
            failWithMessage("Expected stsTokenDuration to be <%s> but was <%s>", stsTokenDuration, actualStsTokenDuration);
        }

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasId(String id) {
        new StandardCredentialsAssert(actual).hasId(id);

        return this;
    }

    public AmazonWebServicesCredentialsAssert hasSameDescriptorIconAs(AmazonWebServicesCredentials theirs) {
        new StandardCredentialsAssert(actual).hasSameDescriptorIconAs(theirs);

        return this;
    }
}
