package io.github.renatolsjf.chassis.util.build;

import java.lang.reflect.Field;
import java.util.List;

public class FieldExtractor extends MemberExtractor<Field, FieldExtractor> {

    public FieldExtractor(List<Field> members) {
        super(members);
    }

}
