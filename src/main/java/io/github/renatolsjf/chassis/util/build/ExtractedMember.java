package io.github.renatolsjf.chassis.util.build;

import java.lang.reflect.Member;

public abstract class ExtractedMember<T extends Member> {

    protected Object object;
    protected T member;

    public ExtractedMember(Object object, T member) {
        this.object = object;
        this.member = member;
    }

    public abstract void set(Object... params);
}
