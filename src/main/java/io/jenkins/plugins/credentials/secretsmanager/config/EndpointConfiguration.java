package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.AmazonClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;

import hudson.util.FormValidation;

import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.io.Serializable;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public class EndpointConfiguration extends AbstractDescribableImpl<EndpointConfiguration>
        implements Serializable {
    private static final long serialVersionUID = 1L;

    private String serviceEndpoint;
    private String signingRegion;

    @DataBoundConstructor
    public EndpointConfiguration(String serviceEndpoint, String signingRegion) {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public String getSigningRegion() {
        return signingRegion;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setSigningRegion(String signingRegion) {
        this.signingRegion = signingRegion;
    }

    @Override
    public String toString() {
        return "Service Endpoint = " + serviceEndpoint + ", Signing Region = " + signingRegion;
    }

    @Extension
    @Symbol("endpointConfiguration")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<EndpointConfiguration> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.endpointConfiguration();
        }

        /**
         * Test the endpoint configuration.
         *
         * @param serviceEndpoint the AWS service endpoint e.g. http://localhost:4584
         * @param signingRegion the AWS signing region e.g. us-east-1
         * @return a success or failure indicator
         */
        @POST
        @SuppressWarnings("unused")
        public FormValidation doTestEndpointConfiguration(
                @QueryParameter("serviceEndpoint") final String serviceEndpoint,
                @QueryParameter("signingRegion") final String signingRegion) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            final AwsClientBuilder.EndpointConfiguration ec =
                    new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion);
            final AWSSecretsManager client =
                    AWSSecretsManagerClient.builder().withEndpointConfiguration(ec).build();

            final int statusCode;
            try {
                statusCode = client.listSecrets(new ListSecretsRequest())
                        .getSdkHttpMetadata()
                        .getHttpStatusCode();
            } catch (AmazonClientException ex) {
                final String msg = Messages.awsClientError() + ": '" + ex.getMessage() + "'";
                return FormValidation.error(msg);
            }

            if ((statusCode >= 200) && (statusCode <= 399)) {
                return FormValidation.ok(Messages.success());
            } else {
                return FormValidation.error(Messages.awsServerError() + ": HTTP " + statusCode);
            }
        }
    }
}
