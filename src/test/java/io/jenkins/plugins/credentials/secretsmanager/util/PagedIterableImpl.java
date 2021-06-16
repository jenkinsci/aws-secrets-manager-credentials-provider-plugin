package io.jenkins.plugins.credentials.secretsmanager.util;

import org.kohsuke.github.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

public class PagedIterableImpl<T> extends PagedIterable<T> {

    private final Class<T[]> receiverType;
    private final Consumer<T> itemInitializer;
    private final Iterator iterator;

    public PagedIterableImpl(Class<T[]> receiverType, Consumer<T> itemInitializer, Iterator iterator) {
        this.receiverType = receiverType;
        this.itemInitializer = itemInitializer;
        this.iterator = iterator;
    }

    @Nonnull
    public PagedIterator<T> _iterator(int pageSize) {
        return null;
    }

}
