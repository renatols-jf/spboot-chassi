package io.github.renatolsjf.chassis.util.conversion;

import io.github.renatolsjf.chassis.util.CaseString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConversionFactory {

    public static <T2> Converter<Object, T2> converter(Class<?> before, Class<T2> after) {

        if (before == null || after == null) {
            throw new NullPointerException();
        }

        if (before == after) {
            return (value) -> (T2) value;
        }

        String converterClassName = "io.github.renatolsjf.chassis.util.conversion."
                + CaseString.getValue(CaseString.CaseType.PASCAL, ClassName.parse(before).getParsedName()
                + "To" + ClassName.parse(after).getParsedName()) + "Converter";

        try {
            Class<? extends Converter> converterClass = (Class<? extends Converter>) Class.forName(converterClassName);
            Constructor<? extends Converter> converterConstructor = converterClass.getConstructor();
            return converterConstructor.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return (value) -> value.getClass() == after.getClass()
                    ? (T2) value
                    : null;
        }

    }

}
