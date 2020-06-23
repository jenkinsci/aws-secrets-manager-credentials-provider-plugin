package io.jenkins.plugins.credentials.secretsmanager.util;

public final class FormValidationResult {

    private final boolean success;

    private final String message;

    private FormValidationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static FormValidationResult success(String msg) {
        return new FormValidationResult(true, msg);
    }

    public static FormValidationResult error(String msg) {
        return new FormValidationResult(false, msg);
    }
}