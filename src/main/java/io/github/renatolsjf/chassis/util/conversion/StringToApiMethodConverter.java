package io.github.renatolsjf.chassis.util.conversion;

import io.github.renatolsjf.chassis.integration.dsl.ApiCall;

public class StringToApiMethodConverter implements Converter<String, ApiCall.ApiMethod> {

    @Override
    public ApiCall.ApiMethod convert(String value) {
        try {
            return ApiCall.ApiMethod.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
