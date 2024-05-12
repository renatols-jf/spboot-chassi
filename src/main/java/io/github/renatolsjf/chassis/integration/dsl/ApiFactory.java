package io.github.renatolsjf.chassis.integration.dsl;

import io.github.renatolsjf.chassis.integration.dsl.resttemplate.RestTemplateApiCall;
import io.github.renatolsjf.chassis.util.CaseString;
import io.github.renatolsjf.chassis.util.build.ObjectBuilder;
import io.github.renatolsjf.chassis.util.build.UnableToBuildObjectException;

import java.util.HashMap;
import java.util.Map;

public class ApiFactory {

    private static Map<String, ApiCall> apiCalls = new HashMap<>();

    public static ApiCall createApiCall() {
        return new RestTemplateApiCall();
    }

    public static void initializeApis(Map<String, Object> apiData) {
        ObjectBuilder objectBuilder = new ObjectBuilder();
        apiData.entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> {
                    try {
                        apiCalls.put(CaseString.getValue(CaseString.CaseType.CAMEL, e.getKey()),
                                objectBuilder.build(RestTemplateApiCall.class, (Map) e.getValue()));
                    } catch (UnableToBuildObjectException ex) {}
                });
    }

    public static ApiCall apiFromLabel(String label) {
        String s = CaseString.getValue(CaseString.CaseType.CAMEL, label);
        return apiCalls.get(s);
    }



}