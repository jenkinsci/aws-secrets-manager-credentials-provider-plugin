package io.jenkins.plugins.credentials.secretsmanager.config;

import io.jenkins.plugins.credentials.secretsmanager.util.FormValidationResult;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import io.jenkins.plugins.credentials.secretsmanager.util.FormValidationResult;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckEndpointConfigurationApiIT extends AbstractCheckEndpointConfigurationIT {

    public final JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins);

    @Override
    protected FormValidationResult validate(String serviceEndpoint, String signingRegion) {
        final JenkinsRule.JSONWebResponse response = doPost(
                    String.format("descriptorByName/io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration/testEndpointConfiguration?serviceEndpoint=%s&signingRegion=%s", serviceEndpoint, signingRegion),
                    "");

        final ParsedBody parsedBody = getValidationMessage(response.getContentAsString(StandardCharsets.UTF_8));

        if (parsedBody.status.equals("ok")) {
            return FormValidationResult.success(parsedBody.msg);
        } else {
            return FormValidationResult.error(parsedBody.msg);
        }
    }

    private JenkinsRule.JSONWebResponse doPost(String path, Object json) {
        try {
            return jenkins.postJSON(path, json);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ParsedBody getValidationMessage(String body) {
        final Pattern p = Pattern.compile("<div class=(.+)><img.+>(.+)</div>");
        final Matcher matcher = p.matcher(body);
        if (matcher.find()) {
            final String status = matcher.group(1);
            final String msg = matcher.group(2);
            return new ParsedBody(msg, status);
        } else {
            throw new RuntimeException("Could not parse response body");
        }
    }

    private static class ParsedBody {
        final String msg;
        final String status;

        private ParsedBody(String msg, String status) {
            this.msg = msg;
            this.status = status;
        }
    }
}
