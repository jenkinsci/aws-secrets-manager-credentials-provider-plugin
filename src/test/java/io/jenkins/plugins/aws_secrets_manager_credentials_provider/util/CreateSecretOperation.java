package io.jenkins.plugins.aws_secrets_manager_credentials_provider.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;

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

        return new Result(name, secretString);
    }

    public static class Opts {
        public String description = "";
        public Map<String, String> tags = Collections.emptyMap();
    }

    public static class Result {
        private final String name;
        private final String value;

        public Result(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
