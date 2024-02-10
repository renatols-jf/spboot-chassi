package com.github.renatolsjf.chassi.integration;

public class ServerErrorOperationException extends StatusRestOperationException {

    public ServerErrorOperationException(int status) { super(status); }
    public ServerErrorOperationException(int status, String message) { super(status, message); }
    public ServerErrorOperationException(int status, Throwable cause) { super(status, cause); }
    public ServerErrorOperationException(int status, String message, Throwable cause) { super(status, message, cause); }

}
