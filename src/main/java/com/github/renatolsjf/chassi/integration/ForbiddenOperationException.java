package com.github.renatolsjf.chassi.integration;

public class ForbiddenOperationException extends StatusRestOperationException {

    public ForbiddenOperationException() { super(403); }
    public ForbiddenOperationException(String message) { super(403, message); }
    public ForbiddenOperationException(Throwable cause) { super(403, cause); }
    public ForbiddenOperationException(String message, Throwable cause) { super(403, message, cause); }

}
