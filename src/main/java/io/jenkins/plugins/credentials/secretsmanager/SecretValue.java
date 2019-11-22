package io.jenkins.plugins.credentials.secretsmanager;

import java.io.Serializable;

abstract class SecretValue implements Serializable {

    private SecretValue() {

    }

    static SecretValue string(String string) {
        return new SecretString(string);
    }

    static SecretValue binary(byte[] bytes) {
        return new SecretBinary(bytes);
    }

    public abstract <R> R match(Matcher<R> matcher);

    public interface Matcher<R> {
        R string(String str);

        R binary(byte[] bytes);
    }

    private static final class SecretString extends SecretValue {

        private static final long serialVersionUID = 1L;

        private final String string;

        private SecretString(String string) {
            this.string = string;
        }

        @Override
        public <R> R match(Matcher<R> matcher) {
            return matcher.string(string);
        }
    }

    private static final class SecretBinary extends SecretValue {

        private static final long serialVersionUID = 1L;

        private final byte[] bytes;

        private SecretBinary(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public <R> R match(Matcher<R> matcher) {
            return matcher.binary(bytes);
        }
    }
}
