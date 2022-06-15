package io.jenkins.plugins.credentials.secretsmanager.config.transformations.description;

import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

public class WebDescriptionIT extends AbstractDescriptionIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setHide() {
        r.configure(form -> {
            final PluginConfigurationForm f = new PluginConfigurationForm(form);

            final HtmlSelect select = f.getDropdownList("Description");
            select.getOptionByText("Hide").setSelected(true);
        });
    }
}
