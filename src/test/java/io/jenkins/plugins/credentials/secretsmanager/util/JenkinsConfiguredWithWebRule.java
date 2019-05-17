package io.jenkins.plugins.credentials.secretsmanager.util;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jvnet.hudson.test.JenkinsRule;

import java.util.function.Consumer;

public class JenkinsConfiguredWithWebRule extends JenkinsRule {

    public void configure(Consumer<HtmlForm> configurator) {
        final JenkinsRule.WebClient webClient = super.createWebClient();

        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setCssEnabled(false);

        try {
            final HtmlPage p = webClient.goTo("configure");

            final HtmlForm form = p.getFormByName("config");

            configurator.accept(form);

            super.submit(form);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure Jenkins");
        }
    }
}
