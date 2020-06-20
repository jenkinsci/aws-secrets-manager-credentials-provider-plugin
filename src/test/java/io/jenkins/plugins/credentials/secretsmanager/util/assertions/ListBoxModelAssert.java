package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import hudson.util.ListBoxModel;
import org.assertj.core.api.AbstractAssert;

public class ListBoxModelAssert extends AbstractAssert<ListBoxModelAssert, ListBoxModel> {
    public ListBoxModelAssert(ListBoxModel options) {
        super(options, ListBoxModelAssert.class);
    }

    public ListBoxModelAssert containsOption(String name, String value) {
        isNotNull();

        final boolean yes = actual.stream()
                .anyMatch(option -> (option.name.equals(name)) && option.value.equals(value));

        if (!yes) {
            failWithMessage("Did not find an option with name <%s> and value <%s>", name, value);
        }

        return this;
    }
}
