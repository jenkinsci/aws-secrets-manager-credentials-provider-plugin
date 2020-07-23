package io.jenkins.plugins.credentials.secretsmanager.config;

import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.*;
import io.jenkins.plugins.casc.impl.attributes.MultivaluedAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return Sets.newHashSet(
                new MultivaluedAttribute<Beta, String>("roles", String.class)
                        .setter((target, roleArns) -> {
                            final List<ARN> arns = roleArns.stream().map(ARN::new).collect(Collectors.toList());
                            target.setRoles(new Roles(arns));
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