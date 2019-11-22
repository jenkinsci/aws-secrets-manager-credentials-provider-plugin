package io.jenkins.plugins.credentials.secretsmanager.util;

public abstract class Strings {
    private Strings() {

    }

    /**
     * Approximates a multiline string in Java.
     *
     * @param lines the lines to concatenate with a newline separator
     * @return the concatenated multiline string
     */
    public static String m(String... lines) {
        return String.join("\n", lines);
    }
}
