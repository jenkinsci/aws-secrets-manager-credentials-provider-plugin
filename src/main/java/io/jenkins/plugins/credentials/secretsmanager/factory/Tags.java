package io.jenkins.plugins.credentials.secretsmanager.factory;

/**
 * The tags on the Secrets Manager entry that we use.
 */
public abstract class Tags {
    private static final String namespace = "jenkins:credentials:";

    public static final String filename = namespace + "filename";
    public static final String type = namespace + "type";
    public static final String username = namespace + "username";
    public static final String accesskeyid = namespace + "accesskeyid";
    public static final String iamrolearn = namespace + "iamrolearn";
    public static final String iamexternalid = namespace + "iamexternalid";
    public static final String iammfaserialnumberid = namespace + "iammfaserialnumberid";
    public static final String ststokenduration = namespace + "ststokenduration";
    private Tags() {

    }
}
