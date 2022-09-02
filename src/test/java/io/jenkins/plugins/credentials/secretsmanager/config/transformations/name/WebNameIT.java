package io.jenkins.plugins.credentials.secretsmanager.config.transformations.name;

import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

import java.util.Set;

public class WebNameIT extends AbstractNameIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setRemovePrefix(String prefix) {
        r.configure(form -> {
            final PluginConfigurationForm f = new PluginConfigurationForm(form);

            final HtmlSelect select = f.getDropdownList("Name");
            select.getOptionByText("Remove Prefix").setSelected(true);
            form.getInputByName("_.prefix").setValueAttribute(prefix);
        });
    }

    @Override
    protected void setRemovePrefixes(Set<Value> prefixes) {
        r.configure(form -> {
            final PluginConfigurationForm f = new PluginConfigurationForm(form);

            final HtmlSelect select = f.getDropdownList("Name");
            select.getOptionByText("Remove Prefixes").setSelected(true);

            // TODO support multiple prefix values
            // this is a hack that skips the 'other' _.value input in the form
            final String firstPrefix = prefixes.stream().findFirst().get().getValue();
            form.getInputsByName("_.value").get(0).setValueAttribute(firstPrefix);
            // FIXME address form.getInputByName("_.prefixes").setValueAttribute(prefix);
        });
    }
}
