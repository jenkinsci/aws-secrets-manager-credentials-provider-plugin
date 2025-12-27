package io.jenkins.plugins.credentials.secretsmanager.factory;

import java.util.Optional;

public interface Tags {
    Optional<String> get(String key);
}
