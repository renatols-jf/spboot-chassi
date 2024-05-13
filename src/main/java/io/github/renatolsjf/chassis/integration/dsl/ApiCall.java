package io.github.renatolsjf.chassis.integration.dsl;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.Renderable;
import io.github.renatolsjf.chassis.util.build.BuildIgnore;
import io.github.renatolsjf.chassis.util.build.Buildable;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Buildable(ignoreContaining = {"get", "post", "put", "patch", "delete", "execute"})
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

    protected Map<String, String> headers = new HashMap<>();

    protected boolean followRedirect = true;
    @BuildIgnore
    protected Duration connectTimeOut = Duration.ofSeconds(10);
    @BuildIgnore
    protected Duration readTimeOut = Duration.ofSeconds(40);

    protected boolean failOnError = true;

    private String endpoint;
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> urlReplacements = new HashMap<>();
    private ApiMethod method;


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

    public ApiCall withConnectTimeoutSeconds(long seconds) {
        this.connectTimeOut = Duration.ofSeconds(seconds);
        return this;
    }

    public ApiCall withReadTimeOutSeconds(long seconds) {
        this.readTimeOut = Duration.ofSeconds(seconds);;
        return this;
    }

    public ApiCall withFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    public ApiCall withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ApiCall withQueryParam(String key, String value) {
        this.queryParams.put(key, value);
        return this;
    }

    public ApiCall withUrlReplacement(String key, String value) {
        this.urlReplacements.put(key, value);
        return this;
    }

    public ApiCall withApiMethod(ApiMethod apiMethod) {
        this.method = apiMethod;
        return this;
    }

    protected String getEndpoint() {

        if (this.endpoint == null) {
            throw new NullPointerException("Endpoint not set");
        }

        String url = this.endpoint;

        if (!this.queryParams.isEmpty()) {
            String queryString;
            if (this.endpoint.contains("?")) {
                queryString = "&";
            } else {
                queryString = "?";
            }
            queryString += queryParams.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            url += queryString;
        }

        for (Map.Entry<String, String> entry : this.urlReplacements.entrySet()) {
            url = url.replaceAll("{" + entry.getKey() + "}", entry.getValue());
        }

        return url;
    }

    public ApiCall withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public ApiCall withBasicAuth(String username, String password) {
        String authHeader = username + ":" + password;
        this.headers.put("Authorization", "Basic " + new String(Base64.getEncoder().encode(authHeader.getBytes())));
        return this;
    }

    public ApiCall withBearerToken(String token) {
        this.headers.put("Authorization", "Bearer " + token);
        return this;
    }


    //---------- GET
    public ApiResponse get() throws ApiException {
        return this.execute(ApiMethod.GET, null);
    }

    //---------- POST
    public ApiResponse post() throws ApiException {
        return this.post((Object) null);
    }

    public ApiResponse post(Renderable body) throws ApiException {
        return this.post(Media.ofRenderable(body).render());
    }

    public ApiResponse post(Renderable... body) throws ApiException {
        return this.post(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse post(T body) {
        return this.execute(ApiMethod.POST, body);
    }

    //---------- PUT
    public ApiResponse put() throws ApiException {
        return this.put((Object) null);
    }

    public ApiResponse put(Renderable body) throws ApiException {
        return this.put(Media.ofRenderable(body).render());
    }

    public ApiResponse put(Renderable... body) throws ApiException {
        return this.put(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse put(T body) {
        return this.execute(ApiMethod.PUT, body);
    }

    //---------- PATCH
    public ApiResponse patch() throws ApiException {
        return this.patch((Object) null);
    }

    public ApiResponse patch(Renderable body) throws ApiException {
        return this.patch(Media.ofRenderable(body).render());
    }

    public ApiResponse patch(Renderable... body) throws ApiException {
        return this.patch(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse patch(T body) {
        return this.execute(ApiMethod.PATCH, body);
    }

    //---------- DELETE
    public ApiResponse delete() throws ApiException {
        return this.delete((Object) null);
    }

    public ApiResponse delete(Renderable body) throws ApiException {
        return this.delete(Media.ofRenderable(body).render());
    }

    public ApiResponse delete(Renderable... body) throws ApiException {
        return this.delete(Media.ofCollection(body).render());
    }

    protected <T> ApiResponse delete(T body) throws ApiException {
        return this.execute(ApiMethod.DELETE, body);
    }

    public ApiResponse execute() throws ApiException {
        return this.execute((Object) null);
    }

    public ApiResponse execute(Renderable body) throws ApiException {
        return this.execute(Media.ofRenderable(body).render());
    }

    public ApiResponse execute(Renderable... body) throws ApiException {
        return this.execute(Media.ofCollection(body).render());
    }

    public <T> ApiResponse execute(T body) throws ApiException {
        if (this.method == null) {
            throw new NullPointerException("Api method not set");
        }
        return this.execute(this.method, body);
    }

    private <T> ApiResponse execute(ApiMethod method, T body) throws ApiException {

        TimedOperation<ApiResponse> timedOperation =
                TimedOperation.http();

        ApiResponse apiResponse = timedOperation.execute(() -> this.doExecute(method, body));
        long duration = timedOperation.getExecutionTimeInMillis();
        String statusCode = apiResponse.getHttpStatus();

        Context.forRequest().createLogger()
                .info("API CALL: " + method.toString() + " " + this.getEndpoint() +
                        " " + statusCode + " " + duration)
                .attach(LOGGING_FIELD_PROVIDER, this.provider)
                .attach(LOGGING_FIELD_SERVICE, this.service)
                .attach(LOGGING_FIELD_OPERATION, this.operation)
                .attach(LOGGING_FIELD_ENDPOINT, this.getEndpoint())
                .attach(LOGGING_FIELD_METHOD, method.toString())
                .attach(LOGGING_FIELD_CONNECTION_ERROR, apiResponse.isConnectionError())
                .attach(LOGGING_FIELD_REQUEST_ERROR, apiResponse.isRequestError())
                .attach(LOGGING_FIELD_HTTP_STATUS, statusCode)
                .attach(LOGGING_FIELD_REQUEST_HEADERS, this.headers)
                .attach(LOGGING_FIELD_REQUEST_BODY, body)
                .attach(LOGGING_FIELD_RESPONSE_HEADERS, apiResponse.getHeaders())
                .attach(LOGGING_FIELD_RESPONSE_BODY, apiResponse.getRawBody())
                .attach(LOGGING_FIELD_REQUEST_DURATION, duration)
                .log();

        Chassis.getInstance().getApplicationHealthEngine().httpCallEnded(this.provider, this.service, this.operation,
                statusCode, apiResponse.isSuccess(), apiResponse.isClientError(), apiResponse.isServerError(), duration);

        if (apiResponse.isConnectionError() && this.failOnError) {

            throw new IOApiException("Unknown error on http call", apiResponse.getCause());
        } else if (apiResponse.isUnauthorized()) {
            Context.forRequest().createLogger()
                    .error("Unauthorized http request")
                    .log();
        } else if (apiResponse.isForbidden()) {
            Context.forRequest().createLogger()
                    .error("Forbidden http request")
                    .log();
        }

        if (!apiResponse.isSuccess()) {
            Context.forRequest().createLogger()
                    .error("Unknown error in http call: statusCode -> {}", statusCode)
                    .log();

            if (failOnError) {
                throw RequestErrorApiException.create(apiResponse);
            }
        }

        return apiResponse;

    }

    protected abstract <T> ApiResponse doExecute(ApiMethod method, T Body);


}
