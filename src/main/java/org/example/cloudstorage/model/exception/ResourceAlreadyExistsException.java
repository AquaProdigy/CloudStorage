package org.example.cloudstorage.model.exception;

import org.example.cloudstorage.util.PathUtil;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
