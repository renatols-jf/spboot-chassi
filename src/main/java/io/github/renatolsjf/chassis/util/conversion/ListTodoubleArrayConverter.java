package io.github.renatolsjf.chassis.util.conversion;

import java.util.List;

public class ListTodoubleArrayConverter implements Converter<List, double[]>{

    @Override
    public double[] convert(List value) {

        double[] doubleArray = new double[value.size()];
        for (int i = 0; i < value.size(); i++) {

            Object o = value.get(i);
            if (o == null) {
                return null;
            }

            Double d = ConversionFactory.converter(o.getClass(), Double.class).convert(o);
            if (d == null) {
                return null;
            } else {
                doubleArray[i] = d;
            }

        }

        return doubleArray;

    }

}
