package com.github.renatolsjf.chassi.integration;

import com.github.renatolsjf.chassi.Chassi;
import com.github.renatolsjf.chassi.MetricRegistry;
import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.monitoring.timing.TimedOperation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Map;

public class RestOperation {

    private static final String REQUEST_DURATION_METRIC_NAME = "integration_request_millis";

    private static ObjectMapper mapper = new ObjectMapper();

    private String group;
    private String service;
    private String operation;

    private String uri;
    private Map<String, String> headers;
    private Map<String, Object> body;
    private HttpMethod method;

    private boolean followRedirect = true;
    private Duration connectTimeOut = Duration.ofSeconds(10);
    private Duration readTimeOut = Duration.ofSeconds(40);

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static RestOperation get(String group, String service, String operation,
                                    String uri, Map<String, String> headers, Map<String, Object> body) {
        return create(group, service, operation, uri, headers, body, HttpMethod.GET);
    }

    public static RestOperation post(String group, String service, String operation,
                                     String uri, Map<String, String> headers, Map<String, Object> body) {
        return create(group, service, operation, uri, headers, body, HttpMethod.POST);
    }

    public static RestOperation patch(String group, String service, String operation,
                                     String uri, Map<String, String> headers, Map<String, Object> body) {
        return create(group, service, operation, uri, headers, body, HttpMethod.PATCH);
    }

    public static RestOperation put(String group, String service, String operation,
                                    String uri, Map<String, String> headers, Map<String, Object> body) {
        return create(group, service, operation, uri, headers, body, HttpMethod.PUT);
    }

    public static RestOperation delete(String group, String service, String operation,
                                    String uri, Map<String, String> headers, Map<String, Object> body) {
        return create(group, service, operation, uri, headers, body, HttpMethod.DELETE);
    }

    private static RestOperation create(String group, String service, String operation,
                                        String uri, Map<String, String> headers, Map<String, Object> body, HttpMethod method) {
        RestOperation ro = new RestOperation();
        ro.group = group;
        ro.service = service;
        ro.operation = operation;
        ro.uri = uri;
        ro.headers = headers;
        ro.body = body;
        ro.method = method;
        return ro;
    }

    public RestOperation withFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    public ResponseEntity<String> callForResponseEntity() {
        return this.callForResponseEntity(Map.class);
    }

    public ResponseEntity<String> callForResponseEntity(Class<?> errorResponseType) throws OperationException {

        HttpHeaders h = new HttpHeaders();
        if (this.headers != null) {
            this.headers.keySet().forEach(k -> h.set(k, this.headers.get(k)));
        }

        HttpEntity he = new HttpEntity(this.body, h);
        RestTemplate r = new RestTemplateBuilder()
                .requestFactory(() -> new SimpleClientHttpRequestFactory(){
                    @Override
                    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                        super.prepareConnection(connection, httpMethod);
                        connection.setInstanceFollowRedirects(RestOperation.this.followRedirect);
                    }
                })
                .setConnectTimeout(this.connectTimeOut)
                .setReadTimeout(this.readTimeOut)
                .build();

        ResponseEntity<String> re;
        TimedOperation<ResponseEntity<String>> timedOperation =
                TimedOperation.http();
        try {
            re = timedOperation.execute(() -> r.exchange(this.uri, this.method, he, String.class));
        } catch (HttpClientErrorException | HttpServerErrorException e) {

            long duration = timedOperation.getExecutionTimeInMillis();
            String statusCode = String.valueOf(e.getStatusCode().value());
            String stringBody = e.getResponseBodyAsString();

            Context.forRequest().createLogger()
                    .info("API CALL: " + this.method.toString() + " " + this.uri +
                            " " + statusCode + " " + duration)
                    .attach("group", this.group)
                    .attach("service", this.service)
                    .attach("operation", this.operation)
                    .attach("endpoint", this.uri)
                    .attach("method", this.method.toString())
                    .attach("connectionError", false)
                    .attach("requestError", true)
                    .attach("httpStatus", statusCode)
                    .attach("requestHeaders", this.headers)
                    .attach("requestBody", this.body)
                    .attach("responseHeaders", e.getResponseHeaders())
                    .attach("responseBody", stringBody)
                    .attach("requestDuration", duration)
                    .log();

            /*ApplicationHealthEngine.addRestBasedRequestData(this.group, this.service, this.operation,
                    duration, e.getStatusCode().value());*/

            Chassi.getInstance().getMetricRegistry().createBuilder(REQUEST_DURATION_METRIC_NAME)
                    .withTag("type", "rest")
                    .withTag("group", this.group)
                    .withTag("service", this.service)
                    .withTag("operation", this.operation)
                    .withTag("outcome", String.valueOf(e.getStatusCode().value()))
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(timedOperation.getExecutionTimeInMillis());

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                Context.forRequest().createLogger()
                        .error("Unauthorized http request")
                        .log();
                throw new UnauthorizedOperationException();
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                Context.forRequest().createLogger()
                        .error("Forbidden http request")
                        .log();
                throw new UnauthorizedOperationException();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Object errorResponse = Try
                    .of(() -> objectMapper.readValue(e.getResponseBodyAsString(), errorResponseType))
                    .getOrNull();

            Context.forRequest().createLogger()
                    .error("Unknown error in http call: statusCode -> {}", statusCode)
                    .log();

            if (e.getStatusCode().is4xxClientError()) {
                throw new ClientErrorOperationException(e.getStatusCode().value()).withBody(errorResponse);
            } else {
                throw new ServerErrorOperationException(e.getStatusCode().value()).withBody(errorResponse);
            }

        } catch(Exception e) {

            long duration = timedOperation.getExecutionTimeInMillis();

            Context.forRequest().createLogger()
                    .info("API CALL: " + this.method.toString() + " " + this.uri +
                            " CONNECTION_ERROR " + duration)
                    .attach("group", this.group)
                    .attach("service", this.service)
                    .attach("operation", this.operation)
                    .attach("endpoint", this.uri)
                    .attach("method", this.method.toString())
                    .attach("connectionError", true)
                    .attach("requestError", false)
                    .attach("httpStatus", null)
                    .attach("requestHeaders", this.headers)
                    .attach("requestBody", this.body)
                    .attach("responseHeaders", null)
                    .attach("responseBody", null)
                    .attach("requestDuration", duration)
                    .log();

            //ApplicationHealthEngine.addRestBasedRequestDataWithConnectionError(this.group, this.service,
                    //this.operation, duration);

            Chassi.getInstance().getMetricRegistry().createBuilder(REQUEST_DURATION_METRIC_NAME)
                    .withTag("type", "rest")
                    .withTag("group", this.group)
                    .withTag("service", this.service)
                    .withTag("operation", this.operation)
                    .withTag("outcome", "connection_error")
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(timedOperation.getExecutionTimeInMillis());

            throw new IOErrorOperationException("Unknown error on http call", e);

        }

        long duration = timedOperation.getExecutionTimeInMillis();
        String statusCode = String.valueOf(re.getStatusCode().value());
        String stringBody = re.getBody();//Try.of(() -> mapper.writeValueAsString(re.getBody())).getOrElse("");

        Context.forRequest().createLogger()
                .info("API CALL: " + this.method.toString() + " " + this.uri+
                        " " + statusCode + " " + duration)
                .attach("group", this.group)
                .attach("service", this.service)
                .attach("operation", this.operation)
                .attach("endpoint", this.uri)
                .attach("method", this.method.toString())
                .attach("connectionError", false)
                .attach("requestError", false)
                .attach("httpStatus", statusCode)
                .attach("requestHeaders", this.headers)
                .attach("requestBody", this.body)
                .attach("responseHeaders", re.getHeaders())
                .attach("responseBody", stringBody)
                .attach("requestDuration", duration)
                .log();

        //ApplicationHealthEngine.addRestBasedRequestData(this.group, this.service, this.operation,
                //duration, re.getStatusCode().value());

        Chassi.getInstance().getMetricRegistry().createBuilder(REQUEST_DURATION_METRIC_NAME)
                .withTag("type", "rest")
                .withTag("group", this.group)
                .withTag("service", this.service)
                .withTag("operation", this.operation)
                .withTag("outcome", String.valueOf(re.getStatusCode().value()))
                .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                .observe(timedOperation.getExecutionTimeInMillis());

        return re;

    }

    public <T> T call(Class<T> returnType) {
        ResponseEntity<String> re = this.callForResponseEntity();
        if (returnType != null) {
            return Try.of(() -> mapper.readValue(re.getBody(), returnType))
                    .getOrElseThrow(t -> new ResponseParsingException("Error while parsing response : " +
                            t.getMessage(), t));
        } else {
            return null;
        }
    }

    public <T> T call(Class<T> returnType, Class<?> errorType) {
        ResponseEntity<String> re = this.callForResponseEntity(errorType);
        if (returnType != null) {
            return Try.of(() -> mapper.readValue(re.getBody(), returnType))
                    .getOrElseThrow(t -> new ResponseParsingException("Error while parsing response : " +
                            t.getMessage(), t));
        } else {
            return null;
        }
    }

}

