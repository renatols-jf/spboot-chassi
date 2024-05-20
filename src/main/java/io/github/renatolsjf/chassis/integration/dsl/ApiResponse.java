package io.github.renatolsjf.chassis.integration.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.renatolsjf.chassis.integration.ResponseParsingException;
import io.github.renatolsjf.chassis.monitoring.timing.TimeSensitive;
import io.vavr.control.Try;

import java.util.Map;

public class ApiResponse implements TimeSensitive {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private int statusCode = 0;
    private String body;
    private Map<String, String> headers;
    private long durationInMillis;

    private Throwable cause;

    public ApiResponse(Throwable cause) {
        this.cause = cause;
    }

    public ApiResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
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
        return this.durationInMillis;
    }

    public String getHttpStatus() {
        return this.isConnectionError()
                ? "CONNECTION_ERROR"
                : String.valueOf(this.statusCode);
    }

    public int getHttpStatusAsInt() {
        return this.statusCode;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public <T> T getBody(Class<T> type) {
        return Try.of(() -> objectMapper.readValue(this.body, type))
                .getOrElseThrow(t -> new ResponseParsingException("Error while parsing response : " +
                        t.getMessage(), t));
    }

    @Override
    public void setDurationInMilliseconds(long milliseconds) {
        this.durationInMillis = milliseconds;
    }
}
