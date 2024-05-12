package io.github.renatolsjf.chassis.util.build;

import io.github.renatolsjf.chassis.util.conversion.ConversionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtractedMethod extends ExtractedMember<Method> {


    public ExtractedMethod(Object object, String memberName, Method member) {
        super(object, memberName, member);
    }

    @Override
    protected void doSet() throws UnableToSetMemberException {
        try {
            this.member.trySetAccessible();
            this.member.invoke(this.object, this.params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnableToSetMemberException(e);
        }
    }

    @Override
    protected void setParams(Object... params) {

        List<Class<?>> paramTypes = Arrays.asList(this.member.getParameterTypes());
        if (paramTypes.size() == params.length
                && paramTypes.containsAll(Arrays.asList(params).stream().map(p -> p.getClass()).collect(Collectors.toList()))) {
            this.params = params;
            this.affinity = 0xF0 & affinity;
        } else if (paramTypes.size() > 1 && params.length == 1 && params[0] instanceof Collection<?>) {
            this.setParams(((Collection<?>) params[0]).toArray());
        } else if (paramTypes.size() == params.length) {
            if (IntStream.range(0, paramTypes.size())
                    .allMatch(i -> ConversionFactory.isConverterAvailable(params[i].getClass(), paramTypes.get(i)))) {
                this.params = IntStream.range(0, paramTypes.size())
                        .mapToObj(i -> ConversionFactory.converter(params[i].getClass(), paramTypes.get(i)).convert(params[i]))
                        .collect(Collectors.toList()).toArray();
                this.affinity = 0x90 & affinity;
            }
        }

    }


}
