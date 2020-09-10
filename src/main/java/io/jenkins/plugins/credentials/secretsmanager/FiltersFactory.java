package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.Filter;
import com.amazonaws.services.secretsmanager.model.FilterNameStringType;
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
        return new Filter()
                .withKey(FilterNameStringType.fromValue(config.getKey()))
                .withValues(convert(config.getValues()));
    }

    private static Collection<String> convert(Collection<Value> config) {
        return config.stream()
                .map(Value::getValue)
                .collect(Collectors.toList());
    }
}
