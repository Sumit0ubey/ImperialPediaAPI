package com.imperialpedia.api.exception;

import jakarta.persistence.EntityExistsException;

public class ResourceAlreadyExistsException extends EntityExistsException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
