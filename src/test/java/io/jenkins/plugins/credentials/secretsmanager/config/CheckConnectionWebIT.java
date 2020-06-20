package io.jenkins.plugins.credentials.secretsmanager.config;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CheckConnectionWebIT extends AbstractCheckConnectionIT {

    public final JenkinsConfiguredWithWebRule jenkins = new JenkinsConfiguredWithWebRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins);

    @Override
    protected Result validate(String serviceEndpoint, String signingRegion) {

        AtomicReference<Result> result = new AtomicReference<>();

        jenkins.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
            form.getInputByName("_.signingRegion").setValueAttribute(signingRegion);

            try {
                getValidateButton(form).click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                final HtmlElement successElement = form.getOneHtmlElementByAttribute("div", "class", "ok");
                result.set(Result.success(successElement.getTextContent()));
                return;
            } catch (ElementNotFoundException ignored) {
                // Carry on
            }

            final HtmlElement failureElement = form.getOneHtmlElementByAttribute("div", "class", "error");
            if (failureElement != null) {
                result.set(Result.error(failureElement.getTextContent()));
            }
        });

        return result.get();
    }

    private static HtmlButton getValidateButton(DomNode node) {
        return node.getByXPath("//span[contains(string(@class),'validate-button')]//button")
                .stream()
                .map(obj -> (HtmlButton) (obj))
                .filter(button -> button.getTextContent().equals("Test Connection"))
                .collect(Collectors.toList())
                .get(0);
    }
}
