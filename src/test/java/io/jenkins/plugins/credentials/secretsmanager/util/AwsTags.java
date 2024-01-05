package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.services.secretsmanager.model.Tag;

/**
 * Tags that the Jenkins plugin looks for.
 */
public abstract class AwsTags {

    private AwsTags() {

    }

    public static Tag filename(String filename) {
        return tag(namespaced("filename"), filename);
    }

    public static Tag username(String username) {
        return tag(namespaced("username"), username);
    }

    public static Tag type(String type) {
        return tag(namespaced("type"), type);
    }

    public static Tag tag(String key, String value) {
        return new Tag().withKey(key).withValue(value);
    }

    private static String namespaced(String key) {
        return "jenkins:credentials:" + key;
    }
}
