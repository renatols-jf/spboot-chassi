package io.github.renatolsjf.chassi.request;

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

    public boolean isSuccessful() {
        return this.successful;
    }

    public boolean isClientFault() {
        return this.clientFault;
    }

    public boolean isServerFault() {
        return this.serverFault;
    }

}
