package io.github.renatolsjf.chassis.util.conversion;

import io.github.renatolsjf.chassis.Configuration;

public class StringToHealthValueTypeConverter implements Converter<String, Configuration.HealthValueType> {

    @Override
    public Configuration.HealthValueType convert(String value) {
        try {
            return Configuration.HealthValueType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
