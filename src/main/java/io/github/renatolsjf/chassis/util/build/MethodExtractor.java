package io.github.renatolsjf.chassis.util.build;

import io.github.renatolsjf.chassis.util.CaseString;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MethodExtractor extends MemberExtractor<Method, MethodExtractor> {

    private List<String> acceptablePrefixes = new ArrayList<>();
    private List<Class<?>> acceptableParameters = new ArrayList<>();

    public MethodExtractor(List<Method> members) {
        super(members);
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
    public ExtractedMember<Method> mostAdequate(Object... parameters) throws NoAdequateMemberException {

        List<ExtractedMember<Method>> extractedMembers = this.get();
        int affinity = 0x00;
        for (ExtractedMember<Method> extractedMethod : extractedMembers) {
            int currentAffinity = 0x00;

        }

        throw new NoAdequateMemberException();

    }

    @Override
    protected ExtractedMember<Method> createExtractedMember(Method member) {
        return null;
    }


}
