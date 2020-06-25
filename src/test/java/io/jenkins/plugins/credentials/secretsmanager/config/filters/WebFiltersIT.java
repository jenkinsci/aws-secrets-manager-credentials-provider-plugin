package io.jenkins.plugins.credentials.secretsmanager.config.filters;

import io.jenkins.plugins.credentials.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

import java.io.IOException;

public class WebFiltersIT extends AbstractFiltersIT {
    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setFilters(Filter... filters) {
        r.configure(form -> {
            final Filter filter = filters[0];

            form.getInputByName("_.listSecrets").setChecked(true);

            // TODO support multiple filters
            final PluginConfigurationForm f = new PluginConfigurationForm(form);
            f.getRepeatableAddButtons("Filters")
                    .stream()
                    .findFirst()
                    .ifPresent(button -> {
                        try {
                            button.click();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            form.getSelectByName("_.key").setSelectedAttribute(filter.getKey(), true);
            // TODO support multiple filter values
            // FIXME this is a hack that skips the 'other' _.value input in the form
            form.getInputsByName("_.value").get(0).setValueAttribute(filter.getValues().get(0).getValue());
        });
    }
}