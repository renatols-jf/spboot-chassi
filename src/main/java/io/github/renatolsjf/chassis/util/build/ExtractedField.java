package io.github.renatolsjf.chassis.util.build;

import io.github.renatolsjf.chassis.util.conversion.ConversionFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExtractedField extends ExtractedMember<Field> {

    public ExtractedField(Object object, String memberName, Field member) {
        super(object, memberName, member);
    }

    @Override
    protected void doSet() throws UnableToSetMemberException {
        this.member.trySetAccessible();
        try {
            this.member.set(this.object, this.params[0]);
        } catch (IllegalAccessException e) {
            throw new UnableToSetMemberException(e);
        }
    }

    @Override
    protected void setParams(Object... params) {

        if (params.length > 1) {
            return;
        }

        Class<?> paramType = this.member.getType();
        if (params.length == 1 && paramType == params[0].getClass()) {
            this.params = params;
            this.affinity = 0xF0 & affinity;
        } else if (params[0] instanceof Collection<?>) {
            this.setParams(((Collection<?>) params[0]).toArray());
        } else if (ConversionFactory.isConverterAvailable(params[0].getClass(), paramType)) {
            this.params = Arrays.asList(params).stream()
                    .map(p -> ConversionFactory.converter(p.getClass(), paramType).convert(p))
                    .collect(Collectors.toList())
                    .toArray();
            this.affinity = 0x90 & affinity;
        }
    }

}
