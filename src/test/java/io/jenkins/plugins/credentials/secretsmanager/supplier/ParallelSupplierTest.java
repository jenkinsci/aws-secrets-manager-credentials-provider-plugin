package io.jenkins.plugins.credentials.secretsmanager.supplier;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ParallelSupplierTest {

    private static final Supplier<Integer> ONE = () -> 1;
    private static final Supplier<Integer> TWO = () -> 2;
    private static final Supplier<Integer> THREE = () -> 3;

    private static <T> Supplier<Collection<T>> newParallelSupplier(Supplier<T>... suppliers) {
        final Collection<Supplier<T>> supplierCollection = Arrays.asList(suppliers);
        return new ParallelSupplier<>(supplierCollection);
    }

    @Test
    public void shouldSupplyNothing() {
        final Supplier<Collection<Integer>> supplier = newParallelSupplier();

        assertThat(supplier.get()).isEmpty();
    }

    @Test
    public void shouldSupplyOneThing() {
        final Supplier<Collection<Integer>> supplier = newParallelSupplier(ONE);

        assertThat(supplier.get()).containsExactly(1);
    }

    @Test
    public void shouldSupplyMultipleThings() {
        final Supplier<Collection<Integer>> supplier = newParallelSupplier(ONE, TWO, THREE);

        assertThat(supplier.get()).containsExactly(1, 2, 3);
    }
}
