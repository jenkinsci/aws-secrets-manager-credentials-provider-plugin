package io.jenkins.plugins.credentials.secretsmanager.util;

import software.amazon.awssdk.services.secretsmanager.model.Tag;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;

/**
 * Tags that the Jenkins plugin looks for.
 */
public abstract class AwsTags {
    private AwsTags() {

    }

    public static Tag filename(String filename) {
        return AwsTags.tag(Tags.filename, filename);
    }

    public static Tag username(String username) {
        return AwsTags.tag(Tags.username, username);
    }

    public static Tag type(String type) {
        return tag(Tags.type, type);
    }

    public static Tag tag(String key) {
        return Tag.builder().key(key).build();
    }

    public static Tag tag(String key, String value) {
        return Tag.builder().key(key).value(value).build();
    }
}
