package io.github.renatolsjf.chassis.util.genesis;

import io.github.renatolsjf.utils.string.casestring.CaseString;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MemberExtractor<T extends Member, I extends MemberExtractor> {

    protected Object object;
    protected List<T> members;
    protected String nameFilter;
    protected List<ExtractorFilter<T>> extractorFilters = new ArrayList<>();

    public MemberExtractor(Object object, List<T> members) {
        this.object = object;
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

    public ExtractedMember<T>  mostAdequate(Object... parameters) throws NoAdequateMemberException {

        List<ExtractedMember<T>> extractedMembers = this.get();
        if (extractedMembers.isEmpty()) {
            throw new NoAdequateMemberException();
        }

        return extractedMembers.stream()
                .map(em -> em.withParams(parameters))
                .filter(ExtractedMember::hasAffinity)
                .sorted()
                .findFirst()
                .orElseThrow(NoAdequateMemberException::new);

    }

    protected abstract ExtractedMember<T> createExtractedMember(T member);

}

