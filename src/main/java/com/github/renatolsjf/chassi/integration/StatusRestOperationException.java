package com.github.renatolsjf.chassi.integration;

public class StatusRestOperationException extends OperationException {

    protected final int status;
    protected Object errorBody;

    protected StatusRestOperationException(int status) {
        super();
        this.status = status;
    }

    protected StatusRestOperationException(int status, String message) {
        super(message);
        this.status = status;
    }

    protected StatusRestOperationException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    protected  StatusRestOperationException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public <T extends OperationException> T withBody(Object errorBody) {
        this.errorBody = errorBody;
        return (T) this;
    }

    public int getStatus() {
        return this.status;
    }

    public Object getErrorBody() {
        return this.errorBody;
    }

    public <T> T getErrorBody(Class<T> clazz) {
        if (clazz.isInstance(this.errorBody)) {
            return (T) this.errorBody;
        } else {
            return null;
        }
    }


}
