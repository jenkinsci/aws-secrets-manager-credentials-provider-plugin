package io.jenkins.plugins.credentials.secretsmanager.supplier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class Lists {

    private Lists() {

    }

    static <T> Map<String, String> toMap(List<T> things, Function<? super T, ? extends String> keyMapper, Function<? super T, ? extends String> valueMapper) {
        return Optional.ofNullable(things).orElse(Collections.emptyList()).stream()
                .filter(tag -> (keyMapper.apply(tag) != null) && (valueMapper.apply(tag) != null))
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }
}
