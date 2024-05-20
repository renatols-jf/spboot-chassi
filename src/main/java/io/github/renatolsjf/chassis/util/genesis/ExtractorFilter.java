package io.github.renatolsjf.chassis.util.genesis;

import java.lang.reflect.Member;

public interface ExtractorFilter<T extends Member> {

    boolean appliesTo(T list);

}
