package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CreateSecretOperation {

    private final AWSSecretsManager client;

    public CreateSecretOperation(AWSSecretsManager client) {
        this.client = client;
    }

    public Result run(String name, String secretString) {
        return run(name, secretString, o -> {});
    }

    public Result run(String name, String secretString, Consumer<Opts> opts) {
        final Opts o = new Opts();
        opts.accept(o);

        final String description = o.description;
        final Map<String, String> tags = o.tags;

        final List<Tag> t = tags.entrySet().stream()
                .map((entry) -> new Tag().withKey(entry.getKey()).withValue(entry.getValue()))
                .collect(Collectors.toList());

        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(name)
                .withDescription(description)
                .withSecretString(secretString)
                .withTags(t);

        final CreateSecretResult result = client.createSecret(request);

        if (result.getSdkHttpMetadata().getHttpStatusCode() >= 400) {
            throw new RuntimeException("Failed to create secret.");
        }

        return new Result(name);
    }

    public Result run(String name, byte[] secretBinary) {
        return run(name, secretBinary, o -> {});
    }

    public Result run(String name, byte[] secretBinary, Consumer<Opts> opts) {
        final Opts o = new Opts();
        opts.accept(o);

        final String description = o.description;
        final Map<String, String> tags = o.tags;

        final List<Tag> t = tags.entrySet().stream()
                .map((entry) -> new Tag().withKey(entry.getKey()).withValue(entry.getValue()))
                .collect(Collectors.toList());

        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(name)
                .withDescription(description)
                .withSecretBinary(ByteBuffer.wrap(secretBinary))
                .withTags(t);

        final CreateSecretResult result = client.createSecret(request);

        if (result.getSdkHttpMetadata().getHttpStatusCode() >= 400) {
            throw new RuntimeException("Failed to create secret.");
        }

        return new Result(name);
    }

    public static class Opts {
        public String description = "";
        public Map<String, String> tags = Collections.emptyMap();
    }

    public static class Result {
        private final String name;

        public Result(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }
}
