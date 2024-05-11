package io.github.renatolsjf.chassis.util.build;

import io.github.renatolsjf.chassis.util.CaseString;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MemberExtractor<T extends Member, I extends MemberExtractor> {

    protected List<T> members;

    protected String nameFilter;
    protected List<ExtractorFilter<T>> extractorFilters = new ArrayList<>();

    public MemberExtractor(List<T> members) {
        this.members = members;
        this.extractorFilters.add((t) -> this.nameFilter == null || t.getName().equals(CaseString.getValue(CaseString.CaseType.CAMEL, this.nameFilter)));
    }

    public List<ExtractedMember<T>> get() {
        return this.members.stream()
                .filter(m -> this.extractorFilters.stream().anyMatch(f -> f.appliesTo(m)))
                .map(m -> this.createExtractedMember(m))
                .collect(Collectors.toList());
    }

    public I withName(String nameFilter) {
        this.nameFilter = nameFilter;
        return (I) this;
    }

    public ExtractedMember<T> mostAdequateOrNull(Object... parameters) {
        try {
            return this.mostAdequate(parameters);
        } catch (NoAdequateMemberException ex) {
            return null;
        }
    }

    public abstract ExtractedMember<T>  mostAdequate(Object... parameters) throws NoAdequateMemberException;
    protected abstract ExtractedMember<T> createExtractedMember(T member);

}

