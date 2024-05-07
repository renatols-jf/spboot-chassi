package io.github.renatolsjf.chassis.integration.dsl;

public class ApiFactory {

    public static ApiCall createApiCall() {
        return new RestTemplateApiCall();
    }

}