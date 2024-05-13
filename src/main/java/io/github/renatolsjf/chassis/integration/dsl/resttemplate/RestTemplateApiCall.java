package io.github.renatolsjf.chassis.integration.dsl.resttemplate;

import io.github.renatolsjf.chassis.integration.OperationException;
import io.github.renatolsjf.chassis.integration.dsl.ApiCall;
import io.github.renatolsjf.chassis.integration.dsl.ApiResponse;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class RestTemplateApiCall extends ApiCall {

    @Override
    protected <T> ApiResponse doExecute(ApiMethod method, T body) throws OperationException {

        HttpHeaders h = new HttpHeaders();
        this.headers.keySet().forEach(k -> h.set(k, this.headers.get(k)));

        HttpEntity<T> he = new HttpEntity(body, h);
        RestTemplate r = new RestTemplateBuilder()
                .requestFactory(() -> new SimpleClientHttpRequestFactory(){
                    @Override
                    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                        super.prepareConnection(connection, httpMethod);
                        connection.setInstanceFollowRedirects(RestTemplateApiCall.this.followRedirect);
                    }
                })
                .setConnectTimeout(this.connectTimeOut)
                .setReadTimeout(this.readTimeOut)
                .build();

        ResponseEntity<String> re;
        ApiResponse apiResponse;

        HttpMethod httpMethod = switch (method) {
            case GET -> HttpMethod.GET;
            case POST -> HttpMethod.POST;
            case PUT -> HttpMethod.PUT;
            case PATCH -> HttpMethod.PATCH;
            case DELETE -> HttpMethod.DELETE;
        };

        try {
            re = r.exchange(this.getEndpoint(), httpMethod, he, String.class);
            apiResponse = new ApiResponse(re.getStatusCode().value(), re.getBody(), re.getHeaders().toSingleValueMap());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            apiResponse = new ApiResponse(e.getStatusCode().value(), e.getResponseBodyAsString(),
                    e.getResponseHeaders() != null ? e.getResponseHeaders().toSingleValueMap() : null);
        } catch (Exception e) {
            apiResponse = new ApiResponse(e);
        }

        return apiResponse;

    }


}
