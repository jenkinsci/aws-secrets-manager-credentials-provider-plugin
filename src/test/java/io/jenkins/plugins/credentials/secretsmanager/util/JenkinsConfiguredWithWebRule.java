package io.jenkins.plugins.credentials.secretsmanager.util;

import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;

import org.jvnet.hudson.test.JenkinsRule;

import java.util.function.Consumer;

public class JenkinsConfiguredWithWebRule extends JenkinsRule {

    public void configure(Consumer<HtmlForm> configurator) {
        try (var webClient = super.createWebClient()) {

            try {
                final HtmlPage p = webClient.goTo("configure");

                final HtmlForm form = p.getFormByName("config");

                configurator.accept(form);

                super.submit(form);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to configure Jenkins", ex);
            }
        }
    }
}
