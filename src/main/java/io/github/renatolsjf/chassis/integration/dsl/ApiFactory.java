package io.github.renatolsjf.chassis.integration.dsl;

import io.github.renatolsjf.chassis.integration.dsl.resttemplate.RestTemplateApiCall;

import java.util.HashMap;
import java.util.Map;

public class ApiFactory {

    private static Map<String, ApiCall> apiCalls = new HashMap<>();

    public static ApiCall createApiCall() {
        return new RestTemplateApiCall();
    }

    public static void initializeApis() {

    }



}