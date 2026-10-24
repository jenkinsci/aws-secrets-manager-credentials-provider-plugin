package io.jenkins.plugins.credentials.secretsmanager.config;

import org.junit.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClientCacheTest {

    private final List<StubClient> built = new ArrayList<>();

    @Test
    public void shouldReuseClientWhenConfigurationIsUnchanged() {
        final var cache = new ClientCache(this::stubBuild);

        cache.update(new Client(null, null, null, "us-east-1"));
        final var first = cache.get();

        // A different instance with the same settings, as produced by re-saving the configuration
        cache.update(new Client(null, null, null, "us-east-1"));

        assertThat(cache.get()).isSameAs(first);
        assertThat(built).hasSize(1);
        assertThat(built.get(0).closed).isFalse();
    }

    @Test
    public void shouldReplaceAndCloseClientWhenConfigurationChanges() {
        final var cache = new ClientCache(this::stubBuild);

        cache.update(new Client(null, null, null, "us-east-1"));
        final var first = cache.get();

        cache.update(new Client(null, null, null, "eu-west-1"));

        assertThat(cache.get()).isNotSameAs(first);
        assertThat(built).hasSize(2);
        assertThat(built.get(0).closed).isTrue();
        assertThat(built.get(1).closed).isFalse();
    }

    @Test
    public void shouldKeepPreviousClientWhenReplacementFails() {
        final var calls = new AtomicInteger();
        final var cache = new ClientCache(config -> {
            if (calls.incrementAndGet() > 1) {
                throw new IllegalArgumentException("bad config");
            }
            return stubBuild(config);
        });

        cache.update(new Client(null, null, null, "us-east-1"));
        final var first = cache.get();

        assertThatThrownBy(() -> cache.update(new Client(null, null, null, "eu-west-1")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(cache.get()).isSameAs(first);
        assertThat(built.get(0).closed).isFalse();
    }

    @Test
    public void shouldFailToGetClientBeforeFirstUpdate() {
        final var cache = new ClientCache(this::stubBuild);

        assertThatIllegalStateException().isThrownBy(cache::get);
    }

    @Test
    public void shouldCloseClientThenResourcesEvenIfSomeFailToClose() {
        final var order = new ArrayList<String>();
        final var stub = new StubClient(order);
        final AutoCloseable failing = () -> {
            order.add("failing");
            throw new Exception("boom");
        };
        final AutoCloseable ok = () -> order.add("ok");

        final var closing = new Client.Built(stub.client, List.of(failing, ok));

        assertThatThrownBy(closing::close).isInstanceOf(IllegalStateException.class);
        assertThat(order).containsExactly("client", "failing", "ok");
    }

    private Client.Built stubBuild(Client config) {
        final var stub = new StubClient(new ArrayList<>());
        built.add(stub);
        return new Client.Built(stub.client, List.of());
    }

    private static class StubClient {
        final SecretsManagerClient client;
        boolean closed;

        StubClient(List<String> order) {
            this.client = (SecretsManagerClient) Proxy.newProxyInstance(
                    SecretsManagerClient.class.getClassLoader(),
                    new Class<?>[]{SecretsManagerClient.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("close")) {
                            closed = true;
                            order.add("client");
                        }
                        return null;
                    });
        }
    }
}
