package io.jenkins.plugins.credentials.secretsmanager.factory.file;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

public class AwsFileCredentials extends BaseStandardCredentials implements FileCredentials {

    @Nonnull
    private final String fileName;

    @Nonnull
    private final Supplier<SecretBytes> content;

    public AwsFileCredentials(String id, String description, String fileName, Supplier<SecretBytes> content) {
        super(id, description);
        this.fileName = fileName;
        this.content = content;
    }

    @Nonnull
    @Override
    public String getFileName() {
        return fileName;
    }

    @Nonnull
    @Override
    public InputStream getContent() {
        final SecretBytes sb = content.get();
        return new ByteArrayInputStream(sb.getPlainData());
    }

    @Nonnull
    SecretBytes getContentBytes() {
        return content.get();
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.file();
        }

        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return provider instanceof AwsCredentialsProvider;
        }
    }
}
