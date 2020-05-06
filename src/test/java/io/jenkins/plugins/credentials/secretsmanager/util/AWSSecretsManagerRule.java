package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.RestoreSecretRequest;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import org.junit.rules.ExternalResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Wraps client-side access to AWS Secrets Manager in tests.
 */
public class AWSSecretsManagerRule extends ExternalResource {

    // TODO use a unique name
    private static final String BAR = "bar";

    // TODO use a unique name
    public static final String FOO = "foo";

    private transient AWSSecretsManager client;

    @Override
    public void before() {
        client = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4584", "us-east-1"))
                .build();

        for (String secretId: Arrays.asList(FOO, BAR)) {
            restoreSecret(secretId);
            forceDeleteSecret(secretId);
        }
    }

    @Override
    protected void after() {
        client = null;

        super.after();
    }

    public CreateSecretOperation.Result createStringSecret(String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, secretString, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.string);
        });
    }

    public CreateSecretOperation.Result createOtherStringSecret(String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(BAR, secretString, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.string);
        });
    }

    public CreateSecretOperation.Result createUsernamePasswordSecret(String username, String password) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, password, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.usernamePassword,
                    Tags.username, username);
        });
    }

    public CreateSecretOperation.Result createSshUserPrivateKeySecret(String username, String privateKey) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, privateKey, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.sshUserPrivateKey,
                    Tags.username, username);
        });
    }

    public CreateSecretOperation.Result createCertificateSecret(byte[] secretBinary) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(FOO, secretBinary, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.certificate);
        });
    }

    public CreateSecretOperation.Result createFileSecret(byte[] content) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, content, opts -> {
            opts.tags = Collections.singletonMap(Tags.type, Type.file);
        });
    }

    public CreateSecretOperation.Result createFileSecret(String filename, byte[] content) {
        final CreateSecretOperation create = new CreateSecretOperation(client);

        return create.run(FOO, content, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.file,
                    Tags.filename, filename);
        });
    }

    /**
     * Low-level API to create any kind of string secret. Warning: YOU MUST SUPPLY YOUR OWN TYPE TAG!
     */
    public CreateSecretOperation.Result createSecret(String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(FOO, secretString, opts);
    }

    /**
     * Low-level API to create any kind of binary secret. Warning: YOU MUST SUPPLY YOUR OWN TYPE TAG!
     */
    public CreateSecretOperation.Result createSecret(byte[] secretBinary, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(FOO, secretBinary, opts);
    }

    /**
     * Low-level API to create any kind of string secret. Warning: YOU MUST SUPPLY YOUR OWN TYPE TAG!
     */
    public CreateSecretOperation.Result createOtherSecret(String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(client);
        return create.run(BAR, secretString, opts);
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

    public void restoreSecret(String secretId) {
        final RestoreSecretRequest request = new RestoreSecretRequest().withSecretId(secretId);
        try {
            client.restoreSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }

    public void deleteSecret(String secretId) {
        final DeleteSecretRequest request = new DeleteSecretRequest()
                .withSecretId(secretId);

        try {
            client.deleteSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }
}
