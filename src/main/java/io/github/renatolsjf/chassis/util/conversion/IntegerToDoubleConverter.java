package io.github.renatolsjf.chassis.util.conversion;

public class IntegerToDoubleConverter implements Converter<Integer, Double> {

    @Override
    public Double convert(Integer value) {
        return Double.valueOf(value);
    }

}
