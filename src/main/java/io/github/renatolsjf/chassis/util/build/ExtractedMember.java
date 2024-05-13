package io.github.renatolsjf.chassis.util.build;

import io.github.renatolsjf.chassis.util.CaseString;

import java.lang.reflect.Member;
import java.util.Map;

public abstract class ExtractedMember<T extends Member> implements Comparable<ExtractedMember<T>> {

    private static final int AFFINITY_THRESHOLD = 0x11;

    protected static final Map<Class<?>, Class<?>> wrapperTypes = Map.of(boolean.class, Boolean.class,
            byte.class, Byte.class, char.class, Character.class, float.class, Float.class, int.class, Integer.class, long.class, Long.class,
            short.class, Short.class, double.class, Double.class);

    protected Object object;
    protected T member;
    protected Object[] params;
    protected int affinity = 0x00;

    public ExtractedMember(Object object, String memberName, T member) {
        this.object = object;
        this.member = member;

        CaseString cs = CaseString.parse(memberName);
        String camel = cs.getValue(CaseString.CaseType.CAMEL);
        String pascal = cs.getValue(CaseString.CaseType.PASCAL);
        if (this.member.getName().equals(camel)) {
            this.affinity = 0x0F;
        } else if (this.member.getName().equals(pascal)) {
            this.affinity = 0x0F;
        } else if (this.member.getName().equalsIgnoreCase(camel)) {
            this.affinity = 0x09;
        } else if (this.member.getName().toUpperCase().endsWith(pascal.toUpperCase())) {
            this.affinity = 0x01;
        } else {
            this.affinity = 0x00;
        }

    }

    public boolean setAndIgnore() {
        try {
            this.set();
            return true;
        } catch (UnableToSetMemberException ex) {
            return false;
        }
    }

    public void set() throws UnableToSetMemberException {

        if (params.length == 0) {
            throw new UnableToSetMemberException(new IllegalStateException("Params not set"));
        }

        if (this.affinity < AFFINITY_THRESHOLD) {
            throw new UnableToSetMemberException("Provided params can't be used to set member");
        }

        this.doSet();

    }

    @Override
    public int compareTo(ExtractedMember<T> o) {
        return this.affinity - o.affinity;
    }

    public ExtractedMember<T> withParams(Object... params) {
        this.setParams(params);
        return this;
    }

    public boolean hasAffinity() {
        return this.affinity >= AFFINITY_THRESHOLD;
    }

    protected abstract void doSet() throws UnableToSetMemberException;
    protected abstract void setParams(Object... params);





}
