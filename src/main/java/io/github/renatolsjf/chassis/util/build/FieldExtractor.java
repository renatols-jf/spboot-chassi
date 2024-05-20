package io.github.renatolsjf.chassis.util.build;

import java.lang.reflect.Field;
import java.util.List;

public class FieldExtractor extends MemberExtractor<Field, FieldExtractor> {


    public FieldExtractor(Object object, List<Field> members) {
        super(object, members);
    }

    @Override
    protected ExtractedMember<Field> createExtractedMember(Field member) {
        return new ExtractedField(this.object, this.nameFilter, member);
    }
}
