package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.BaseConfigurator;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.impl.attributes.MultivaluedAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes.Prefix;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import java.util.Set;
import java.util.stream.Collectors;

@Extension(optional = true, ordinal = 2)
@Restricted(NoExternalUse.class)
@SuppressWarnings("unused")
public class RemovePrefixesConfigurator extends BaseConfigurator<RemovePrefixes>
        implements Configurator<RemovePrefixes> {

    // NOTE: this MUST be the same as the target class' @Symbol annotation
    @Override
    @NonNull
    public String getName() {
        return "removePrefixes";
    }

    @Override
    public Class<RemovePrefixes> getTarget() {
        return RemovePrefixes.class;
    }

    @Override
    public RemovePrefixes instance(Mapping mapping, ConfigurationContext context) {
        return new RemovePrefixes(null);
    }

    @Override
    @NonNull
    public Set<Attribute<RemovePrefixes, ?>> describe() {
        return Sets.newHashSet(
                new MultivaluedAttribute<RemovePrefixes, String>("prefixes", String.class)
                        .setter((target, prefixes) -> {
                            final Set<Prefix> mappedValues = prefixes.stream().map(Prefix::new).collect(Collectors.toSet());
                            target.setPrefixes(mappedValues);
                        }));
    }

    @CheckForNull
    @Override
    public CNode describe(RemovePrefixes instance, ConfigurationContext context) throws Exception {
        final Mapping mapping = new Mapping();
        for (Attribute<RemovePrefixes, ?> attribute: describe()) {
            mapping.put(attribute.getName(), attribute.describe(instance, context));
        }
        return mapping;
    }

}