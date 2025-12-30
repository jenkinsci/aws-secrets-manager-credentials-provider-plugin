package io.jenkins.plugins.credentials.secretsmanager;

import software.amazon.awssdk.services.secretsmanager.model.Filter;
import software.amazon.awssdk.services.secretsmanager.model.FilterNameStringType;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class FiltersFactory {

    public static Collection<Filter> create(Collection<io.jenkins.plugins.credentials.secretsmanager.config.Filter> config) {
        if (config == null) {
            return Collections.emptyList();
        }

        return config.stream()
                .map(FiltersFactory::create)
                .collect(Collectors.toList());
    }

    private static Filter create(io.jenkins.plugins.credentials.secretsmanager.config.Filter config) {
        return Filter.builder()
                .key(FilterNameStringType.fromValue(config.getKey()))
                .values(convert(config.getValues()))
                .build();
    }

    private static Collection<String> convert(Collection<Value> config) {
        return config.stream()
                .map(Value::getValue)
                .collect(Collectors.toList());
    }
}
