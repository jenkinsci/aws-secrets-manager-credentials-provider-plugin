package io.jenkins.plugins.credentials.secretsmanager.config;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

import org.junit.Ignore;
import org.junit.Rule;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;

@Ignore("pipeline-model-definition breaks the Web config UI with a load order bug between credentials consumers and (remote) providers")
public class CheckConnectionWebIT extends AbstractCheckConnectionIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected Result validate(String serviceEndpoint, String signingRegion) {

        AtomicReference<Result> result = new AtomicReference<>();

        r.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
            form.getInputByName("_.signingRegion").setValueAttribute(signingRegion);

            try {
                getValidateButton(form).click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final HtmlElement successElement = form.getOneHtmlElementByAttribute("div", "class", "ok");
            if (successElement != null) {
                result.set(Result.success(successElement.getTextContent()));
                return;
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
