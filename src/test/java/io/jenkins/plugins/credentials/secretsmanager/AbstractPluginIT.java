package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.RestoreSecretRequest;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.Maps;
import org.apache.commons.lang3.SerializationUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation.Result;

public abstract class AbstractPluginIT {

    // TODO use a unique name
    private static final String BAR = "bar";

    // TODO use a unique name
    private static final String FOO = "foo";

    private final AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4584", "us-east-1"))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")))
            .build();

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private CredentialsStore store;

    @BeforeClass
    public static void fakeAwsCredentials() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretKey", "test");
    }

    @Before
    public void setup() {
        store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();

        for (String secretId: Arrays.asList(FOO, BAR)) {
            restoreSecret(secretId);
            forceDeleteSecret(secretId);
        }
    }

    WorkflowRun runPipeline(String definition) {
        try {
            final WorkflowJob project = r.jenkins.createProject(WorkflowJob.class, "example");
            project.setDefinition(new CpsFlowDefinition(definition, true));
            final QueueTaskFuture<WorkflowRun> workflowRunFuture = project.scheduleBuild2(0);
            final WorkflowRun workflowRun = workflowRunFuture.waitForStart();
            r.waitForCompletion(workflowRun);

            return workflowRun;
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    <C extends Credentials> List<C> lookupCredentials(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, r.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    <C extends IdCredentials> ListBoxModel listCredentials(Class<C> type) {
        return CredentialsProvider.listCredentials(type, r.jenkins, null, null, null);
    }

    <C extends IdCredentials> List<String> lookupCredentialNames(Class<C> type) {
        final ListBoxModel result = CredentialsProvider.listCredentials(type, r.jenkins, ACL.SYSTEM, null, null);

        return result.stream()
            .map(o -> o.name)
            .collect(Collectors.toList());
    }

    Result createStringSecret(String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, secretString, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.string);
        });
    }

    Result createOtherStringSecret(String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(BAR, secretString, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.string);
        });
    }

    Result createUsernamePasswordSecret(String username, String password) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, password, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.usernamePassword,
                    Tags.username, username);
        });
    }

    Result createSshUserPrivateKeySecret(String username, String privateKey) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, privateKey, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.sshUserPrivateKey,
                    Tags.username, username);
        });
    }

    Result createCertificateSecret(byte[] secretBinary) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(FOO, secretBinary, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.certificate);
        });
    }

    Result createFileSecret(String fileName, byte[] content) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, content, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.file,
                    Tags.filename, fileName);
        });
    }

    /**
     * Low-level API to create any kind of string secret. Warning: YOU MUST SUPPLY YOUR OWN TYPE TAG!
     */
    Result createSecret(String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(FOO, secretString, opts);
    }

    /**
     * Low-level API to create any kind of string secret. Warning: YOU MUST SUPPLY YOUR OWN TYPE TAG!
     */
    Result createOtherSecret(String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(BAR, secretString, opts);
    }

    void deleteSecret(String secretId) {
        final DeleteSecretRequest request = new DeleteSecretRequest()
                .withSecretId(secretId);

        try {
            client.deleteSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }

    CredentialsStore store() {
        return this.store;
    }

    private void forceDeleteSecret(String secretId) {
        final DeleteSecretRequest request = new DeleteSecretRequest()
                .withSecretId(secretId)
                .withForceDeleteWithoutRecovery(true);

        try {
            client.deleteSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }

    private void restoreSecret(String secretId) {
        final RestoreSecretRequest request = new RestoreSecretRequest().withSecretId(secretId);
        try {
            client.restoreSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }

    <C extends StandardCredentials> C lookupCredential(Class<C> type, String id) {
        return lookupCredentials(type).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected a credential but none was present"));
    }

    static <C extends StandardCredentials> C snapshot(C credentials) {
        return SerializationUtils.deserialize(SerializationUtils.serialize(CredentialsProvider.snapshot(credentials)));
    }
}
