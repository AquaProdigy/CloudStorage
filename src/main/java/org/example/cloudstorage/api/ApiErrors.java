package org.example.cloudstorage.api;

public enum ApiErrors {
    UNEXPECTED_EXCEPTION("Unexpected Exception"),
    RESOURCE_NOT_FOUND("Resource not found: %s"),
    RESOURCE_ALREADY_EXISTS("Resource already exists: %s"),
    INVALID_PATH("Invalid path: %s"),
    QUERY_IS_BLANK("Query is blank"),
    USER_NOT_AUTHENTICATED("User not authenticated"),
    USER_ALREADY_AUTHENTICATED("User is already authenticated"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    BAD_CREDENTIALS("Bad credentials"),
    ;

    private final String message;

    ApiErrors(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
