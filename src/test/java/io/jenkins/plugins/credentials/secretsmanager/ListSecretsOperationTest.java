package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CancelRotateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CancelRotateSecretResult;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteResourcePolicyRequest;
import com.amazonaws.services.secretsmanager.model.DeleteResourcePolicyResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.amazonaws.services.secretsmanager.model.DescribeSecretRequest;
import com.amazonaws.services.secretsmanager.model.DescribeSecretResult;
import com.amazonaws.services.secretsmanager.model.GetRandomPasswordRequest;
import com.amazonaws.services.secretsmanager.model.GetRandomPasswordResult;
import com.amazonaws.services.secretsmanager.model.GetResourcePolicyRequest;
import com.amazonaws.services.secretsmanager.model.GetResourcePolicyResult;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ListSecretVersionIdsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretVersionIdsResult;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.PutResourcePolicyRequest;
import com.amazonaws.services.secretsmanager.model.PutResourcePolicyResult;
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.PutSecretValueResult;
import com.amazonaws.services.secretsmanager.model.RestoreSecretRequest;
import com.amazonaws.services.secretsmanager.model.RestoreSecretResult;
import com.amazonaws.services.secretsmanager.model.RotateSecretRequest;
import com.amazonaws.services.secretsmanager.model.RotateSecretResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.amazonaws.services.secretsmanager.model.TagResourceRequest;
import com.amazonaws.services.secretsmanager.model.TagResourceResult;
import com.amazonaws.services.secretsmanager.model.UntagResourceRequest;
import com.amazonaws.services.secretsmanager.model.UntagResourceResult;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretResult;
import com.amazonaws.services.secretsmanager.model.UpdateSecretVersionStageRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretVersionStageResult;

import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ListSecretsOperationTest {

    @Test
    public void shouldHandleMissingSecret() {
        final ListSecretsResult result = new ListSecretsResult().withSecretList();
        final ListSecretsOperation strategy = new ListSecretsOperation(new MockAwsSecretsManager(result));

        final Collection<SecretListEntry> secrets = strategy.get();

        assertThat(secrets).isEmpty();
    }

    @Test
    public void shouldHandleSecret() {
        final ListSecretsResult result = new ListSecretsResult().withSecretList(new SecretListEntry().withName("foo").withDescription("bar"));
        final ListSecretsOperation strategy = new ListSecretsOperation(new MockAwsSecretsManager(result));

        final Collection<SecretListEntry> secrets = strategy.get();

        assertThat(secrets).containsExactly(new SecretListEntry().withDescription("bar").withName("foo"));
    }

    @Test
    public void shouldHandleSecretWithTags() {
        final ListSecretsResult result = new ListSecretsResult().withSecretList(new SecretListEntry().withName("foo").withDescription("bar").withTags(new Tag().withKey("key").withValue("value")));
        final ListSecretsOperation strategy = new ListSecretsOperation(new MockAwsSecretsManager(result));

        final Collection<SecretListEntry> secrets = strategy.get();

        assertThat(secrets).containsExactly(new SecretListEntry().withDescription("bar").withName("foo").withTags(new Tag().withKey("key").withValue("value")));
    }

    private static class MockAwsSecretsManager implements AWSSecretsManager {

        private final ListSecretsResult result;

        private MockAwsSecretsManager(ListSecretsResult result) {
            this.result = result;
        }

        @Override
        public CancelRotateSecretResult cancelRotateSecret(CancelRotateSecretRequest cancelRotateSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CreateSecretResult createSecret(CreateSecretRequest createSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DeleteResourcePolicyResult deleteResourcePolicy(DeleteResourcePolicyRequest deleteResourcePolicyRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DeleteSecretResult deleteSecret(DeleteSecretRequest deleteSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DescribeSecretResult describeSecret(DescribeSecretRequest describeSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetRandomPasswordResult getRandomPassword(GetRandomPasswordRequest getRandomPasswordRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetResourcePolicyResult getResourcePolicy(GetResourcePolicyRequest getResourcePolicyRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetSecretValueResult getSecretValue(GetSecretValueRequest getSecretValueRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListSecretVersionIdsResult listSecretVersionIds(ListSecretVersionIdsRequest listSecretVersionIdsRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListSecretsResult listSecrets(ListSecretsRequest listSecretsRequest) {
            return result;
        }

        @Override
        public PutResourcePolicyResult putResourcePolicy(PutResourcePolicyRequest putResourcePolicyRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PutSecretValueResult putSecretValue(PutSecretValueRequest putSecretValueRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RestoreSecretResult restoreSecret(RestoreSecretRequest restoreSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RotateSecretResult rotateSecret(RotateSecretRequest rotateSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TagResourceResult tagResource(TagResourceRequest tagResourceRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UntagResourceResult untagResource(UntagResourceRequest untagResourceRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpdateSecretResult updateSecret(UpdateSecretRequest updateSecretRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpdateSecretVersionStageResult updateSecretVersionStage(UpdateSecretVersionStageRequest updateSecretVersionStageRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shutdown() {

        }

        @Override
        public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest amazonWebServiceRequest) {
            throw new UnsupportedOperationException();
        }
    }
}
