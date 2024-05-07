package io.github.renatolsjf.chassis.integration.dsl;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.integration.*;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.Renderable;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public abstract class ApiCall {

    public enum ApiMethod {
        GET,
        POST,
        PUT,
        PATCH,
        DELETE
    }

    private static final String LOGGING_FIELD_PROVIDER = "provider";
    private static final String LOGGING_FIELD_SERVICE = "service";
    private static final String LOGGING_FIELD_OPERATION = "operation";
    private static final String LOGGING_FIELD_ENDPOINT = "endpoint";
    private static final String LOGGING_FIELD_METHOD = "method";
    private static final String LOGGING_FIELD_CONNECTION_ERROR = "connectionError";
    private static final String LOGGING_FIELD_REQUEST_ERROR = "requestError";
    private static final String LOGGING_FIELD_HTTP_STATUS = "httpStatus";
    private static final String LOGGING_FIELD_REQUEST_HEADERS = "requestHeaders";
    private static final String LOGGING_FIELD_REQUEST_BODY = "requestBody";
    private static final String LOGGING_FIELD_RESPONSE_HEADERS = "responseHeaders";
    private static final String LOGGING_FIELD_RESPONSE_BODY = "responseBody";
    private static final String LOGGING_FIELD_REQUEST_DURATION = "requestDuration";

    protected String operation;
    protected String service;
    protected String provider;

    protected Map<String, String> headers;
    protected Renderable body;
    protected URI uri;

    protected boolean followRedirect = true;
    protected Duration connectTimeOut = Duration.ofSeconds(10);
    protected Duration readTimeOut = Duration.ofSeconds(40);

    protected boolean failOnError = true;


    public ApiCall withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public ApiCall withService(String service) {
        this.service = service;
        return this;
    }

    public ApiCall withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public ApiCall withFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    public ApiCall withConnectTimeout(Duration connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    public ApiCall withReadTimeOut(Duration readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public abstract ApiCall withHeader(String key, String value);

    public ApiResponse get() throws OperationException {
        return this.execute(ApiMethod.GET, null);
    }



    public ApiResponse post() throws OperationException {
        return this.post((Object) null);
    }

    public ApiResponse post(Renderable body) throws OperationException {
        return this.post(Media.ofRenderable(body).render());
    }

    public ApiResponse post(Renderable... body) throws OperationException {
        return this.post(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse post(T body) {
        return this.execute(ApiMethod.POST, body);
    }



    public ApiResponse put() throws OperationException {
        return this.put((Object) null);
    }

    public ApiResponse put(Renderable body) throws OperationException {
        return this.put(Media.ofRenderable(body).render());
    }

    public ApiResponse put(Renderable... body) throws OperationException {
        return this.put(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse put(T body) {
        return this.execute(ApiMethod.PUT, body);
    }



    public ApiResponse patch() throws OperationException {
        return this.patch((Object) null);
    }

    public ApiResponse patch(Renderable body) throws OperationException {
        return this.patch(Media.ofRenderable(body).render());
    }

    public ApiResponse patch(Renderable... body) throws OperationException {
        return this.patch(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse patch(T body) {
        return this.execute(ApiMethod.PATCH, body);
    }




    public ApiResponse delete() throws OperationException {
        return this.delete((Object) null);
    }

    public ApiResponse delete(Renderable body) throws OperationException {
        return this.delete(Media.ofRenderable(body).render());
    }

    public ApiResponse delete(Renderable... body) throws OperationException {
        return this.delete(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse delete(T body) {
        return this.execute(ApiMethod.DELETE, body);
    }

    protected <T> ApiResponse execute(ApiMethod method, T body) {

        TimedOperation<ApiResponse> timedOperation =
                TimedOperation.http();

        ApiResponse apiResponse = timedOperation.execute(() -> this.doExecute(method, body));
        long duration = timedOperation.getExecutionTimeInMillis();
        String statusCode = apiResponse.getHttpStatus();

        Context.forRequest().createLogger()
                .info("API CALL: " + method.toString() + " " + this.uri +
                        " " + statusCode + " " + duration)
                .attach(LOGGING_FIELD_PROVIDER, this.provider)
                .attach(LOGGING_FIELD_SERVICE, this.service)
                .attach(LOGGING_FIELD_OPERATION, this.operation)
                .attach(LOGGING_FIELD_ENDPOINT, this.uri)
                .attach(LOGGING_FIELD_METHOD, method.toString())
                .attach(LOGGING_FIELD_CONNECTION_ERROR, apiResponse.isConnectionError())
                .attach(LOGGING_FIELD_REQUEST_ERROR, apiResponse.isRequestError())
                .attach(LOGGING_FIELD_HTTP_STATUS, statusCode)
                .attach(LOGGING_FIELD_REQUEST_HEADERS, this.headers)
                .attach(LOGGING_FIELD_REQUEST_BODY, this.body)
                .attach(LOGGING_FIELD_RESPONSE_HEADERS, apiResponse.getHeaders())
                .attach(LOGGING_FIELD_RESPONSE_BODY, apiResponse.getRawBody())
                .attach(LOGGING_FIELD_REQUEST_DURATION, duration)
                .log();

        Chassis.getInstance().getApplicationHealthEngine().httpCallEnded(this.provider, this.service, this.operation,
                statusCode, apiResponse.isSuccess(), apiResponse.isClientError(), apiResponse.isServerError(), duration);

        if (apiResponse.isConnectionError() && this.failOnError) {
            throw new IOErrorOperationException("Unknown error on http call", apiResponse.getCause());
        } else if (apiResponse.isRequestError()) {

            if (apiResponse.isUnauthorized()) {
                Context.forRequest().createLogger()
                        .error("Unauthorized http request")
                        .log();
                if (this.failOnError) {
                    throw new UnauthorizedOperationException();
                }
            } else if (apiResponse.isForbidden()) {
                Context.forRequest().createLogger()
                        .error("Forbidden http request")
                        .log();
                if (this.failOnError) {
                    throw new ForbiddenOperationException();
                }
            }

        }

        Context.forRequest().createLogger()
                .error("Unknown error in http call: statusCode -> {}", statusCode)
                .log();

        if (failOnError) {
            throw apiResponse.isClientError()
                    ? new ClientErrorOperationException(apiResponse.getHttpStatusAsInt())
                    : new ServerErrorOperationException(apiResponse.getHttpStatusAsInt());
        }

        return apiResponse;

    }

    protected abstract <T> ApiResponse doExecute(ApiMethod method, T Body);


}
