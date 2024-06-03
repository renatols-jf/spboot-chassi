package io.github.renatolsjf.chassis.util.conversion;

public class IntegerToLongConverter implements Converter<Integer, Long> {

    @Override
    public Long convert(Integer value) {
        return value != null
                ? value.longValue()
                : null;
    }

}
