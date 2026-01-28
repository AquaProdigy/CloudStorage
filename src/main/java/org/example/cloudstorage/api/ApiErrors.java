package org.example.cloudstorage.api;

public enum ApiErrors {
    UNEXPECTED_EXCEPTION("Unexpected Exception"),
    RESOURCE_NOT_FOUND("Resource not found: %s"),
    RESOURCE_ALREADY_EXISTS("Resource already exists: %s"),
    INVALID_PATH("Invalid path: %s"),
    QUERY_IS_BLANK("Query is blank"),
    ;

    private final String message;

    ApiErrors(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
