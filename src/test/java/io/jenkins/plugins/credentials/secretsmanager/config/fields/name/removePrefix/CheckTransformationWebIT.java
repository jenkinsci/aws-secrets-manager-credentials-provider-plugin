package io.jenkins.plugins.credentials.secretsmanager.config.fields.name.removePrefix;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import io.jenkins.plugins.credentials.secretsmanager.util.FormValidationResult;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CheckTransformationWebIT extends CheckTransformationIT {

    @Override
    protected FormValidationResult validate(String prefix) {
        final AtomicReference<FormValidationResult> result = new AtomicReference<>();

        jenkins.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setRemovePrefixTransformation(prefix);

            final HtmlButton validateButton = form.getValidateButtons("Test Transformation").get(0);
            try {
                validateButton.click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final FormValidationResult r;
            final Optional<String> successMessage = form.getValidateSuccessMessage();
            if (successMessage.isPresent()) {
                r = FormValidationResult.success(successMessage.get());
            } else {
                final String failureMessage = form.getValidateErrorMessage();
                r = FormValidationResult.error(failureMessage);
            }

            result.set(r);
        });

        return result.get();
    }
}
