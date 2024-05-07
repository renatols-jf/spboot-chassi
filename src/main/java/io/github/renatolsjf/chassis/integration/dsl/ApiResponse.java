package io.github.renatolsjf.chassis.integration.dsl;

import java.util.Map;

public class ApiResponse {

    private int statusCode = 0;
    private String body;

    private Throwable cause;

    public ApiResponse(Throwable cause) {
        this.cause = cause;
    }

    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public boolean isRequestError() {
        return !this.isConnectionError() && !this.isSuccess();
    }

    public boolean isConnectionError() {
        return this.statusCode == 0;
    }

    public boolean isSuccess() {
        return this.statusCode >= 200 && this.statusCode < 400;
    }

    public boolean isClientError() {
        return this.statusCode >= 400 && this.statusCode < 500;
    }

    public boolean isServerError() {
        return this.statusCode >= 500;
    }

    public boolean isUnauthorized() {
        return this.statusCode == 401;
    }

    public boolean isForbidden() {
        return this.statusCode == 403;
    }

    public String getRawBody() {
        return this.body;
    }

    public long getDuration() {
        return 0l;
    }

    public String getHttpStatus() {
        return "";
    }

    public int getHttpStatusAsInt() {
        return 0;
    }

    public Map<String, Object> getResponseHeaders() {
        return null;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public <T> T getBody(Class<T> type) {
        return null;
    }




}
