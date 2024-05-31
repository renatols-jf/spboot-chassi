package io.github.renatolsjf.chassis.util.genesis;

import io.github.renatolsjf.chassis.util.conversion.ConversionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtractedMethod extends ExtractedMember<Method> {

    public ExtractedMethod(Object object, String memberName, Method member) {
        super(object, memberName, member);
    }

    @Override
    protected void doCallOrSet() throws UnableToSetMemberException {
        try {
            this.member.trySetAccessible();
            int numberOfParameters = this.member.getParameterTypes().length;
            int numberOfCalls = this.params.length / numberOfParameters;
            for (int i = 0; i < numberOfCalls; i++) {
                this.member.invoke(this.object, Arrays.copyOfRange(this.params,
                        numberOfParameters * i, numberOfParameters * i + numberOfParameters));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnableToSetMemberException(e);
        }
    }

    @Override
    protected Object doCallOrGet() throws UnableToGetMemberException {

        if (this.member.getParameterTypes().length != this.params.length) {
            throw new UnableToGetMemberException("Can't get with multiple parameters");
        }

        this.member.trySetAccessible();
        try {
            return this.member.invoke(this.object, this.params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnableToGetMemberException(e);
        }

    }

    @Override
    protected void setParams(Object... params) {

        List<Class<?>> paramTypes = Arrays.stream(this.member.getParameterTypes())
                .map(c -> wrapperTypes.getOrDefault(c, c)).collect(Collectors.toList());
        if (this.affinity == 0 || (paramTypes.isEmpty() && params.length > 0)) {
            return;
        } else if (params.length == 0 && paramTypes.isEmpty()) {
            this.affinity = 0xF0 | affinity;
        } else if (paramTypes.size() > 1 && params.length == 1 && params[0] instanceof Collection<?>) {
            this.setParams(((Collection<?>) params[0]).toArray());
        } else if (paramTypes.size() <= params.length && params.length % paramTypes.size() == 0) {

            boolean containsNull = Arrays.stream(params).anyMatch(Objects::isNull);
            boolean needsConversion = false;
            boolean isMulti = params.length > paramTypes.size();

            /*boolean isApplicable = IntStream.range(0, params.length)
                    .allMatch(i -> params[i] == null
                            || params[i].getClass() == paramTypes.get(i - ((i / paramTypes.size()) * paramTypes.size()))
                            || ConversionFactory.isConverterAvailable(params[i].getClass(), paramTypes.get((i - ((i / paramTypes.size()) * paramTypes.size())))));
            if (isApplicable) {*/
            Object[] newParams = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Object o = params[i];
                if (o == null || o.getClass() == paramTypes.get(i - ((i / paramTypes.size()) * paramTypes.size()))) {
                    newParams[i] = o;
                } else if (ConversionFactory.isConverterAvailable(params[i].getClass(), paramTypes.get((i - ((i / paramTypes.size()) * paramTypes.size()))))) {
                    needsConversion = true;
                    newParams[i] = ConversionFactory.converter(params[i].getClass(), paramTypes.get((i - ((i / paramTypes.size()) * paramTypes.size())))).convert(params[i]);
                } else {
                    return;
                }
            }
            this.params = newParams;
            this.affinity = 0xF0 | affinity;

            if (isMulti) {
                this.affinity -= 0x60;
            }
            if (containsNull) {
                this.affinity -= 0x30;
            }
            if (needsConversion) {
                this.affinity -= 0x30;
            }
        }



            /*if (paramTypes.containsAll(Arrays.stream(params).map(Object::getClass).collect(Collectors.toList()))) {
                this.params = params;
                this.affinity = 0xF0 | affinity;
            } else if (IntStream.range(0, paramTypes.size())
                    .allMatch(i -> ConversionFactory.isConverterAvailable(params[i].getClass(), paramTypes.get(i)))) {
                this.params = IntStream.range(0, paramTypes.size())
                        .mapToObj(i -> ConversionFactory.converter(params[i].getClass(), paramTypes.get(i)).convert(params[i]))
                        .toArray();
                this.affinity = 0x90 | affinity;
            }

        } else if (params.length > paramTypes.size()
                && params.length % paramTypes.size() == 0
                && this.member.isAnnotationPresent(Multi.class)) {

            int numberOfParameters = paramTypes.size();
            if (paramTypes.containsAll(Arrays.stream(params).map(Object::getClass).collect(Collectors.toList()))) {
                this.params = params;
                this.affinity = 0x90 | affinity;
            } else if (IntStream.range(0, params.length)
                    .allMatch(i -> ConversionFactory.isConverterAvailable(params[i].getClass(), paramTypes.get(i % numberOfParameters)))) {
                this.params = IntStream.range(0, paramTypes.size())
                        .mapToObj(i -> ConversionFactory.converter(params[i].getClass(), paramTypes.get(i)).convert(params[i]))
                        .toArray();
                this.affinity = 0x70 | affinity;
            }

        }*/

    }

}
