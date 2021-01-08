package io.jenkins.plugins.credentials.secretsmanager.config.fields.description;

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import org.junit.Rule;

public class WebDescriptionIT extends AbstractDescriptionIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setDescription(boolean description) {
        r.configure(form -> {
            final HtmlCheckBoxInput enabled = form.getFirstByXPath("//input[@type='checkbox' and @name='_.description']");
            enabled.setChecked(description);
        });
    }
}
