package io.jenkins.plugins.credentials.secretsmanager.config;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.BaseConfigurator;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.CredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Set;

@Extension(optional = true, ordinal = 2)
@Restricted(NoExternalUse.class)
@SuppressWarnings("unused")
public class ClientConfigurator  extends BaseConfigurator<Client>
        implements Configurator<Client> {

    // NOTE: this MUST be the same as the target class' @Symbol annotation
    @Override
    @NonNull
    public String getName() {
        return "client";
    }

    @Override
    public Class<Client> getTarget() {
        return Client.class;
    }

    @Override
    public Client instance(Mapping mapping, ConfigurationContext context) {
        return new Client(new DefaultAWSCredentialsProviderChain(), null, null);
    }

    @Override
    @NonNull
    public Set<Attribute<Client,?>> describe() {
        return ImmutableSet.of(
                new Attribute<Client, CredentialsProvider>("credentialsProvider", CredentialsProvider.class),
                new Attribute<Client, EndpointConfiguration>("endpointConfiguration", EndpointConfiguration.class),
                new Attribute<Client, String>("region", String.class)
                        .setter((target, region) -> {
                            final Region container = new Region(region);
                            target.setRegion(container);
                        }));
    }
}
