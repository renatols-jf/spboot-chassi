package io.github.renatolsjf.chassis.util.genesis;

import io.github.renatolsjf.chassis.util.conversion.ConversionFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

public class ExtractedField extends ExtractedMember<Field> {

    public ExtractedField(Object object, String memberName, Field member) {
        super(object, memberName, member);
    }

    @Override
    protected void doCallOrSet() throws UnableToSetMemberException {

        if (this.params.length == 0) {
            throw new UnableToSetMemberException("Can't set with no parameters");
        }

        this.member.trySetAccessible();
        try {
            this.member.set(this.object, this.params[0]);
        } catch (IllegalAccessException e) {
            throw new UnableToSetMemberException(e);
        }
    }

    @Override
    protected Object doCallOrGet() throws UnableToGetMemberException {
        this.member.trySetAccessible();
        try {
            return this.member.get(this.object);
        } catch (IllegalAccessException e) {
            throw new UnableToGetMemberException(e);
        }
    }

    @Override
    protected void setParams(Object... params) {

        if (params.length > 1) {
            return;
        } else if (params.length == 0) {
            this.affinity = 0x80 | affinity;
            return;
        } else if (params[0] == null) {
            this.affinity = 0x10 | affinity;
            return;
        }

        Class<?> paramType = wrapperTypes.containsKey(this.member.getType())
                ? wrapperTypes.get(this.member.getType())
                :this.member.getType();
        if (params.length == 1 && paramType == params[0].getClass()) {
            this.params = params;
            this.affinity = 0xF0 | affinity;
        } else if (params[0] instanceof Collection<?>) {
            this.setParams(((Collection<?>) params[0]).toArray());
        } else if (ConversionFactory.isConverterAvailable(params[0].getClass(), paramType)) {
            this.params = Arrays.stream(params)
                    .map(p -> ConversionFactory.converter(p.getClass(), paramType).convert(p))
                    .toArray();
            this.affinity = 0x90 | affinity;
        }
    }

}
