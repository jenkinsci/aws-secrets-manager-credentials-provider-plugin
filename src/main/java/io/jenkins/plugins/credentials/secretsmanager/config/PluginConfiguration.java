package io.jenkins.plugins.credentials.secretsmanager.config;

import software.amazon.awssdk.services.secretsmanager.model.FilterNameStringType;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

@Extension
@Symbol("awsCredentialsProvider")
public class PluginConfiguration extends GlobalConfiguration {

    private static final Logger LOG = Logger.getLogger(PluginConfiguration.class.getName());

    /** The Guava cache is never truly turned off, just made very short, as it needs a non-zero cache duration. */
    private static final Duration NO_CACHE = Duration.ofNanos(1);
    private static final Duration DEFAULT_CACHE = Duration.ofSeconds(300);

    /**
     * Whether to cache the credentials or not. By default, credentials are cached for 5 minutes. Caching can be turned off for development purposes.
     */
    private Boolean cache;

    /**
     * Secrets Manager client configuration
     */
    private Client client;

    @Deprecated
    private transient Filters filters;

    private ListSecrets listSecrets;

    private Transformations transformations;

    public PluginConfiguration() {
        load();
    }

    public static PluginConfiguration getInstance() {
        return all().get(PluginConfiguration.class);
    }

    public static Duration normalize(Boolean cache) {
        if (cache == null || cache) {
            LOG.config("CredentialsProvider cache enabled");
            return DEFAULT_CACHE;
        } else {
            LOG.config("CredentialsProvider cache disabled");
            return NO_CACHE;
        }
    }

    protected Object readResolve() {
        if (filters != null && filters.getTag() != null) {
            final var tag = filters.getTag();
            final var tagKey = new Filter(FilterNameStringType.TAG_KEY.toString(), Collections.singletonList(new Value(tag.getKey())));
            final var tagValue = new Filter(FilterNameStringType.TAG_VALUE.toString(), Collections.singletonList(new Value(tag.getValue())));
            final var filters = Arrays.asList(tagKey, tagValue);
            listSecrets = new ListSecrets(filters);
        }

        return this;
    }

    public Boolean getCache() {
        return cache;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setCache(Boolean cache) {
        this.cache = cache;
        save();
    }

    public Client getClient() {
        return client;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setClient(Client client) {
        this.client = client;
        save();
    }

    public ListSecrets getListSecrets() {
        return listSecrets;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setListSecrets(ListSecrets listSecrets) {
        this.listSecrets = listSecrets;
        save();
    }

    public Transformations getTransformations() {
        return transformations;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setTransformations(Transformations transformations) {
        this.transformations = transformations;
        save();
    }

    @Override
    public synchronized boolean configure(StaplerRequest req, JSONObject json) {
        // This method is unnecessary, except to apply the following workaround.
        // Workaround: Set any optional struct fields to null before binding configuration.
        // https://groups.google.com/forum/#!msg/jenkinsci-dev/MuRJ-yPRRoo/AvoPZAgbAAAJ
        this.client = null;
        this.listSecrets = null;
        this.transformations = null;

        req.bindJSON(this, json);
        save();
        return true;
    }
}
