package io.jenkins.plugins.credentials.secretsmanager.util;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jvnet.hudson.test.JenkinsRule;

import java.util.function.Consumer;

public class JenkinsConfiguredWithWebRule extends JenkinsRule {

    public void configure(Consumer<HtmlForm> configurator) {
        final JenkinsRule.WebClient webClient = super.createWebClient();

        // Make ajax synchronous
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        try {
            final HtmlPage p = webClient.goTo("configure");
            webClient.waitForBackgroundJavaScript(5000);

            final HtmlForm form = p.getFormByName("config");

            configurator.accept(form);

            super.submit(form);
            webClient.waitForBackgroundJavaScript(5000);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure Jenkins");
        }
    }
}
