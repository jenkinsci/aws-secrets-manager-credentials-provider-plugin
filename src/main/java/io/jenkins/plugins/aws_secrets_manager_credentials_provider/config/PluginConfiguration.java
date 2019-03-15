package io.jenkins.plugins.aws_secrets_manager_credentials_provider.config;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
@Symbol("awsCredentialsProvider")
public class PluginConfiguration extends GlobalConfiguration {

    /**
     * The AWS Secrets Manager endpoint configuration.
     *
     * If this is null, the default will be used.
     * If this is specified, the user's override will be used.
     */
    private EndpointConfiguration endpointConfiguration;

    private Filters filters;

    public PluginConfiguration() {
        load();
    }

    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setEndpointConfiguration(EndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    public Filters getFilters() {
        return filters;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setFilters(Filters filters) {
        this.filters = filters;
    }
}
