package com.github.renatolsjf.chassi.integration;

public class UnauthorizedOperationException extends StatusRestOperationException {

    public UnauthorizedOperationException() { super(401); }
    public UnauthorizedOperationException(String message) { super(401, message); }
    public UnauthorizedOperationException(Throwable cause) { super(401, cause); }
    public UnauthorizedOperationException(String message, Throwable cause) { super(401, message, cause); }

}
