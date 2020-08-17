package io.jenkins.plugins.credentials.secretsmanager.supplier;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ParallelSupplierTest {

    private static final Supplier<Integer> ONE = () -> 1;
    private static final Supplier<Integer> TWO = () -> 2;
    private static final Supplier<Integer> THREE = () -> 3;

    @Test
    public void shouldSupplyNothing() {
        final Supplier<Collection<Integer>> supplier = new ParallelSupplier<>(Collections.emptyList());

        assertThat(supplier.get()).isEmpty();
    }

    @Test
    public void shouldSupplyOneThing() {
        final Supplier<Collection<Integer>> supplier = new ParallelSupplier<>(Collections.singletonList(ONE));

        assertThat(supplier.get()).containsExactly(1);
    }

    @Test
    public void shouldSupplyMultipleThings() {
        final Supplier<Collection<Integer>> supplier = new ParallelSupplier<>(Arrays.asList(ONE, TWO, THREE));

        assertThat(supplier.get()).containsExactly(1, 2, 3);
    }
}
