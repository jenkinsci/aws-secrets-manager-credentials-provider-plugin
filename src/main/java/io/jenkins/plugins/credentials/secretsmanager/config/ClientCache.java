package io.jenkins.plugins.credentials.secretsmanager.config;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds the one Secrets Manager client that matches the current plugin configuration, so that clients (and their
 * HTTP connection pools and credentials providers) are reused between credential refreshes instead of being rebuilt
 * and leaked every time.
 *
 * <p>The client is only replaced by {@link #update(Client)}, which is called on each refresh. When the configuration
 * has changed, the previous client is closed. Supplying this cache (rather than a client) to credentials makes them
 * pick up the replacement.
 */
public class ClientCache implements Supplier<SecretsManagerClient> {

    private final Function<Client, Client.Built> factory;

    // Guarded by 'this'
    private Object key;
    private Client.Built built;

    public ClientCache() {
        this(Client::build);
    }

    ClientCache(Function<Client, Client.Built> factory) {
        this.factory = factory;
    }

    /**
     * Make the cached client match the given configuration. Keeps the existing client if the configuration is
     * unchanged, otherwise builds a replacement and closes the old client. If building the replacement fails, the old
     * client is kept.
     */
    public synchronized void update(Client config) {
        final var newKey = config.buildKey();
        if (built != null && newKey.equals(key)) {
            return;
        }

        final var replacement = factory.apply(config);
        final var previous = built;

        built = replacement;
        key = newKey;

        if (previous != null) {
            previous.close();
        }
    }

    /**
     * @return the current client
     * @throws IllegalStateException if {@link #update(Client)} has never succeeded
     */
    @Override
    public synchronized SecretsManagerClient get() {
        if (built == null) {
            throw new IllegalStateException("No Secrets Manager client has been built yet");
        }
        return built.client();
    }
}
