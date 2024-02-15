package com.github.renatolsjf.chassi.request;

public enum RequestOutcome {
    SUCCESS(true, false, false),
    CLIENT_ERROR(false, true, false),
    SERVER_ERROR(false, false, true);

    private boolean successful;
    private boolean clientFault;
    private boolean serverFault;

    private RequestOutcome(boolean successful, boolean clientFault, boolean serverFault) {
        this.successful = successful;
        this.clientFault = clientFault;
        this.serverFault = serverFault;
    }

    private boolean isSuccessful() {
        return this.successful;
    }

    private boolean isClientFault() {
        return this.clientFault;
    }

    private boolean isServerFault() {
        return this.serverFault;
    }

}
