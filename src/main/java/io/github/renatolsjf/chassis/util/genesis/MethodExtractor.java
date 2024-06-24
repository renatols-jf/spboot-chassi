package io.github.renatolsjf.chassis.util.genesis;

import io.github.renatolsjf.utils.string.casestring.CaseString;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MethodExtractor extends MemberExtractor<Method, MethodExtractor> {

    private List<String> acceptablePrefixes = new ArrayList<>();
    private List<Class<?>> acceptableParameters = new ArrayList<>();

    public MethodExtractor(Object object, List<Method> members) {
        super(object, members);
        this.extractorFilters.add((m) -> {

            if (this.nameFilter == null || this.acceptablePrefixes.isEmpty()) {
                return true;
            }

            for (String prefix : this.acceptablePrefixes) {
                if (m.getName().equals(prefix + CaseString.getValue(CaseString.CaseType.PASCAL, this.nameFilter))) {
                    if (this.acceptableParameters.isEmpty() ||
                            (acceptableParameters.containsAll(Arrays.asList(m.getParameterTypes()))
                                    && acceptableParameters.size() == m.getParameterTypes().length)) {
                        return true;
                    }
                }
            }

            return false;

        });
    }

    public MethodExtractor getter() {
        this.resetPrefix();
        return this.withPrefix("get")
                .withPrefix("is");
    }

    public MethodExtractor setter() {
        this.resetPrefix();
        return this.withPrefix("set")
                .withPrefix("with");
    }

    public MethodExtractor withPrefix(String prefix) {
        this.acceptablePrefixes.add(prefix);
        return this;
    }

    public MethodExtractor resetPrefix() {
        this.acceptablePrefixes.clear();
        return this;
    }

    public MethodExtractor withParameter(Class<?> type) {
        this.acceptableParameters.add(type);
        return this;
    }

    public MethodExtractor resetParameter() {
        this.acceptableParameters.clear();
        return this;
    }

    @Override
    protected ExtractedMember<Method> createExtractedMember(Method member) {
        return new ExtractedMethod(this.object, this.nameFilter, member);
    }


}
