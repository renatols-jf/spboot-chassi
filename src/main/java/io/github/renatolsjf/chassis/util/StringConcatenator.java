package io.github.renatolsjf.chassis.util;

import java.util.StringJoiner;

public class StringConcatenator {

    private final static String TWO_COLONS = "::";
    private final static String DOT = ".";

    String[] values;

    public StringConcatenator(String... values) {
        this.values = values;
    }

    public String dot() {
        return this.join(DOT);
    }

    public String twoColons() {
        return this.join(TWO_COLONS);
    }

    private String join(String delimiter) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (String value : values) {
            joiner.add(value);
        }
        return joiner.toString();
    }

    public static StringConcatenator of(String... values) {
        return new StringConcatenator(values);
    }

}
