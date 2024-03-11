package io.github.renatolsjf.chassis.filtering;


public class Statement {

    private Statement and;
    private Statement or;

    private boolean negative = false;
    private String field;
    private String valueA;
    private String valueB;
    private Token.TokenType tokenType;

    public Statement getAnd() {
        return and;
    }

    public void setAnd(Statement and) {
        this.and = and;
    }

    public Statement getOr() {
        return or;
    }

    public void setOr(Statement or) {
        this.or = or;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValueA() {
        return valueA;
    }

    public void setValueA(String valueA) {
        this.valueA = valueA;
    }

    public String getValueB() {
        return valueB;
    }

    public void setValueB(String valueB) {
        this.valueB = valueB;
    }

    public Token.TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(Token.TokenType tokenType) {
        this.tokenType = tokenType;
    }
}
