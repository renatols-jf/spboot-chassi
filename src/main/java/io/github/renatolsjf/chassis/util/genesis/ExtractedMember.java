package io.github.renatolsjf.chassis.util.genesis;

import io.github.renatolsjf.utils.string.casestring.CaseString;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public abstract class ExtractedMember<T extends Member> implements Comparable<ExtractedMember<T>> {

    protected static final Map<Class<?>, Class<?>> wrapperTypes = Map.of(boolean.class, Boolean.class,
            byte.class, Byte.class, char.class, Character.class, float.class, Float.class, int.class, Integer.class, long.class, Long.class,
            short.class, Short.class, double.class, Double.class);

    protected Object object;
    protected T member;
    protected Object[] params = new Object[0];
    protected int affinity;

    public ExtractedMember(Object object, String memberName, T member) {
        this.object = object;
        this.member = member;

        if (member instanceof AnnotatedElement el && el.isAnnotationPresent(BuildIgnore.class)) {
            this.affinity = 0x00;
            return;
        }

        if (object.getClass().isAnnotationPresent(Buildable.class)) {
            Buildable buildable = object.getClass().getAnnotation(Buildable.class);
            if (Arrays.stream(buildable.ignoreContaining())
                    .anyMatch(s -> memberName.toUpperCase().contains(s.toUpperCase()))) {
                this.affinity = 0x00;
                return;
            }
        }

        CaseString cs = CaseString.parse(memberName);
        String camel = cs.getCaseValue(CaseString.CaseType.CAMEL);
        String pascal = cs.getCaseValue(CaseString.CaseType.PASCAL);
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

    /*public boolean setAndIgnore() {
        try {
            this.set();
            return true;
        } catch (UnableToSetMemberException ex) {
            return false;
        }
    }*/

    public void callOrSet() throws UnableToSetMemberException {

        /*if (params.length == 0) {
            throw new UnableToSetMemberException(new IllegalStateException("Params not set"));
        }*/

        if (!this.hasAffinity()) {
            throw new UnableToSetMemberException("Provided params can't be used to set member");
        }

        this.doCallOrSet();

    }

    public Object callOrGet() throws UnableToGetMemberException {
        if (!this.hasAffinity()) {
            throw new UnableToGetMemberException("Provided params can't be used to set member");
        }
        return this.doCallOrGet();
    }

    @Override
    public int compareTo(ExtractedMember<T> o) {
        return this.affinity - o.affinity;
    }

    public ExtractedMember<T> withParams(Object... params) {
        params = Arrays.stream(params).filter(Objects::nonNull).toArray();
        this.setParams(params);
        return this;
    }

    public boolean hasAffinity() {
        return (this.affinity & 0xF0) > 0
                && (this.affinity & 0x0F) > 0;
    }

    protected abstract void doCallOrSet() throws UnableToSetMemberException;
    protected abstract Object doCallOrGet() throws UnableToGetMemberException;
    protected abstract void setParams(Object... params);





}
