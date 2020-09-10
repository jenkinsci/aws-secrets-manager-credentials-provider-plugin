package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.services.secretsmanager.model.FilterNameStringType;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.*;
import io.jenkins.plugins.casc.impl.attributes.MultivaluedAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Extension(optional = true, ordinal = 2)
@Restricted(NoExternalUse.class)
@SuppressWarnings("unused")
public class FilterConfigurator extends BaseConfigurator<Filter>
        implements Configurator<Filter> {

    // NOTE: this MUST be the same as the target class' @Symbol annotation
    @Override
    @NonNull
    public String getName() {
        return "filter";
    }

    @Override
    public Class<Filter> getTarget() {
        return Filter.class;
    }

    @Override
    public Filter instance(Mapping mapping, ConfigurationContext context) {
        return new Filter(null, null);
    }

    @Override
    @NonNull
    public Set<Attribute<Filter, ?>> describe() {
        return Sets.newHashSet(
                new Attribute<Filter, String>("key", String.class)
                    .setter((target, key) -> {
                        try {
                            FilterNameStringType.fromValue(key);
                            target.setKey(key);
                        } catch (IllegalArgumentException e) {
                            throw new ConfiguratorException(e.getLocalizedMessage());
                        }
                    }),
                new MultivaluedAttribute<Filter, String>("values", String.class)
                        .setter((target, values) -> {
                            final List<Value> mappedValues = values.stream().map(Value::new).collect(Collectors.toList());
                            target.setValues(mappedValues);
                        }));
    }

    @CheckForNull
    @Override
    public CNode describe(Filter instance, ConfigurationContext context) throws Exception {
        final Mapping mapping = new Mapping();
        for (Attribute<Filter, ?> attribute : describe()) {
            mapping.put(attribute.getName(), attribute.describe(instance, context));
        }
        return mapping;
    }

}