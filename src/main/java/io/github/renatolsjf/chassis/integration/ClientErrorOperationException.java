package io.github.renatolsjf.chassis.integration;

public class ClientErrorOperationException extends StatusRestOperationException {

    public ClientErrorOperationException(int status) { super(status); }
    public ClientErrorOperationException(int status, String message) { super(status, message); }
    public ClientErrorOperationException(int status, Throwable cause) { super(status, cause); }
    public ClientErrorOperationException(int status, String message, Throwable cause) { super(status, message, cause); }

}
