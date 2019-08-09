package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class CredentialProxy implements Serializable, InvocationHandler {

    /**
     * The connection to the remote store, to retrieve the secret portion(s) of the credential.
     */
    private final transient AWSSecretsManager connection;

    /**
     * Non-secret metadata
     */
    private final Map<String, Object> properties;

    /**
     * The secret retrieval method name.
     * <p>
     * Example: for UsernamePassword credential, the secret retrieval method is
     * @see{StandardUsernamePasswordCredentials#getPassword}. To make a proxy credential for it, set
     * secretName to 'password'.
     */
    private final String secretName;

    public CredentialProxy(AWSSecretsManager connection, Map<String, Object> properties, String secretName) {
        this.connection = connection;
        this.properties = properties;
        this.secretName = secretName;
    }

    public static CredentialProxy secretText(AWSSecretsManager client, Map<String, Object> properties) {
        return new CredentialProxy(client, properties, "secret");
    }

    public static CredentialProxy usernamePassword(AWSSecretsManager client, Map<String, Object> properties) {
        return new CredentialProxy(client, properties, "password");
    }

    public static CredentialProxy sshKey(AWSSecretsManager client, Map<String, Object> properties) {
        return new CredentialProxy(client, properties, "privateKey");
    }

    public static CredentialProxy certificate(AWSSecretsManager client, Map<String, Object> properties) {
        return new CredentialProxy(client, properties, "keyStore");
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null || args.length > 0) {
            return null; // (1)
        }
        String n = method.getName();
        if (n.startsWith("get")) {
            n = n.substring(3, 4).toLowerCase() + n.substring(4);
        } else if (n.startsWith("is")) {
            n = n.substring(2, 3).toLowerCase() + n.substring(3);
        } else {
            return null; // (1)
        }
        if (secretName.equals(n)) {
            if (connection != null) {
                // FIXME we'll have to toggle our postprocessing of the secret based on the secretName
                return connection.getSecretValue(...);
            } else {
                throw new IOException("No connection"); // (2)
            }
        } else {
            return properties.get(n);
        }
    }
}