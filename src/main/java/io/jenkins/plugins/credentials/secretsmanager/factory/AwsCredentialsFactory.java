package io.jenkins.plugins.credentials.secretsmanager.factory;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import java.util.Optional;

/**
 * Interface for factories that construct Secrets Manager credential objects. Authors of custom Secrets Manager-based credential types should implement this interface, and declare it in their META-INF/services definition.
 */
public interface AwsCredentialsFactory {

    /**
     * Returns the name of the credential type which this factory is responsible for creating. For example `usernamePassword` or `string`.
     * <p>
     * Only one factory can be responsible for creating a particular credential type. It is an error for multiple factory instances to report the same type name.
     */
    String getType();

    /**
     * Construct a Jenkins credential from a Secrets Manager secret.
     *
     * @param name the secret's name (must be unique within the AWS account)
     * @param description the secret's description
     * @param tags the secret's AWS tags
     * @param client the Secrets Manager client that will retrieve the secret's value on demand
     * @return a credential (if one could be constructed from the secret's properties)
     */
    Optional<StandardCredentials> create(String arn, String name, String description, Tags tags, AWSSecretsManager client);
}
