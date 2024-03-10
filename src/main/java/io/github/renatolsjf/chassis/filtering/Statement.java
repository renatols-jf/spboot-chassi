package io.github.renatolsjf.chassis.filtering;

public class Statement {

    private Statement and;
    private Statement or;

    private boolean negative = false;
    private String field;

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public void setField(String field) {

    }


}
