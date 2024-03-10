package io.github.renatolsjf.chassis.filtering;

import java.util.ArrayList;
import java.util.List;

public class FilterOperation {

    public static String PROPERTY_VALUE_DIVIDER_TOKEN = ":";
    public static String NOT_TOKEN = "NOT";

    public enum Operator {
        AND,
        OR,
        NONE
    }

    public enum Operation {

    }

    private String content;

    private String property;
    private Operation operation;
    private boolean not = false;

    private List<FilterOperation> innerFilters = new ArrayList<>();

    private FilterOperation() {

    }

    /*private void tokenize() {

        if (this.content == null || this.content.isBlank()) {
            this.operation = Operation.NOOP;
            return;
        }

        int idx = this.content.indexOf(PROPERTY_VALUE_DIVIDER_TOKEN);
        if (idx < 1) {
            this.operation = Operation.NOOP;
            return;
        }

        this.property = this.content.substring(0, idx).trim();
        if (this.property.length() >= 4 && this.property.trim().substring())

    }

    public void setContent(String content) {
        this.content = content != null
                ? content.trim()
                : content;
        this.tokenize();
    }

    public static FilterOperation create(String content) {
        FilterOperation fo = new FilterOperation();
        fo.setContent(content);
        fo.tokenize();
        return fo;
    }*/

}
