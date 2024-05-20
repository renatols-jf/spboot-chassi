package io.github.renatolsjf.chassis.util.build;

import java.lang.reflect.Member;

public interface ExtractorFilter<T extends Member> {

    boolean appliesTo(T list);

}
