package io.jenkins.plugins.credentials.secretsmanager.config;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.BaseConfigurator;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.impl.attributes.MultivaluedAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.HashSet;
import java.util.Set;

@Extension(optional = true, ordinal = 2)
@Restricted(NoExternalUse.class)
@SuppressWarnings("unused")
public class BetaConfigurator extends BaseConfigurator<Beta>
        implements Configurator<Beta> {

    // NOTE: this MUST be the same as the target class' @Symbol annotation
    @Override
    @NonNull
    public String getName() {
        return "beta";
    }

    @Override
    public Class<Beta> getTarget() {
        return Beta.class;
    }

    @Override
    public Beta instance(Mapping mapping, ConfigurationContext context) {
        final PluginConfiguration config = PluginConfiguration.all().get(PluginConfiguration.class);

        if (config == null || config.getBeta() == null) {
            // avoid NPE
            return new Beta(null);
        }

        return config.getBeta();
    }

    @Override
    @NonNull
    public Set<Attribute<Beta,?>> describe() {
        return ImmutableSet.of(
                new MultivaluedAttribute<Beta, Client>("clients", Client.class)
                        .setter((target, clients) -> {
                            final Clients container = new Clients(new HashSet<>(clients));
                            target.setClients(container);
                        }));
    }

    @CheckForNull
    @Override
    public CNode describe(Beta instance, ConfigurationContext context) throws Exception {
        Mapping mapping = new Mapping();
        for (Attribute<Beta, ?> attribute : describe()) {
            mapping.put(attribute.getName(), attribute.describe(instance, context));
        }
        return mapping;
    }

}