package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.google.common.io.ByteStreams;
import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public class FileCredentialsAssert extends AbstractAssert<FileCredentialsAssert, FileCredentials> {

    public FileCredentialsAssert(FileCredentials actual) {
        super(actual, FileCredentialsAssert.class);
    }

    public FileCredentialsAssert hasFileName(String fileName) {
        isNotNull();

        if (!Objects.equals(actual.getFileName(), fileName)) {
            failWithMessage("Expected file name to be <%s> but was <%s>", fileName, actual.getFileName());
        }

        return this;
    }

    public FileCredentialsAssert hasContent(byte[] content) {
        isNotNull();

        try {
            final byte[] actualContent = ByteStreams.toByteArray(actual.getContent());

            if (!Arrays.equals(actualContent, content)) {
                failWithMessage("Expected content to be <%s> but was <%s>", content, actualContent);
            }
        } catch (IOException e) {
            failWithMessage("Could not get file credential's content");
        }

        return this;
    }

    public FileCredentialsAssert hasContent(InputStream content) {
        isNotNull();

        try {
            final byte[] expectedContent = ByteStreams.toByteArray(content);
            final byte[] actualContent = ByteStreams.toByteArray(actual.getContent());

            if (!Arrays.equals(actualContent, expectedContent)) {
                failWithMessage("Expected content to be <%s> but was <%s>", expectedContent, actualContent);
            }
        } catch (IOException e) {
            failWithMessage("Could not get file credential's content");
        }

        return this;
    }

    public FileCredentialsAssert hasId(String id) {
        new StandardCredentialsAssert(actual).hasId(id);

        return this;
    }

    public FileCredentialsAssert hasSameDescriptorIconAs(FileCredentials theirs) {
        new StandardCredentialsAssert(actual).hasSameDescriptorIconAs(theirs);

        return this;
    }
}
