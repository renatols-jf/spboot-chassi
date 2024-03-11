package io.github.renatolsjf.chassis.filtering;


public class Statement {

    private Statement left;
    private Statement child;
    private Statement parent;

    private Statement and;
    private Statement or;

    private boolean negative = false;
    private boolean isString = false;
    private boolean isStringClosed = false;
    private String field;
    private String valueA;
    private String valueB;
    private Token.TokenType tokenType;

    public Statement getLeft() {
        return left;
    }

    public void setLeft(Statement left) {
        this.left = left;
    }

    public Statement getChild() {
        return child;
    }

    public void setChild(Statement child) {
        this.child = child;
        child.setParent(this);
    }

    public Statement getParent() {
        return parent;
    }

    protected void setParent(Statement parent) {
        this.parent = parent;
        //parent.setChild(this);
    }

    public Statement getAnd() {
        return and;
    }

    public void setAnd(Statement and) {
        this.and = and;
        this.and.setLeft(this);
        this.and.setParent(this.parent);
    }

    public Statement getOr() {
        return or;
    }

    public void setOr(Statement or) {
        this.or = or;
        this.or.setLeft(this);
        this.or.setParent(this.parent);
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public boolean isString() {
        return isString;
    }

    public void setString(boolean string) {
        isString = string;
    }

    public boolean isStringClosed() {
        return isStringClosed;
    }

    public void setStringClosed(boolean stringClosed) {
        isStringClosed = stringClosed;
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

    public boolean isEmpty() {
        return !this.negative && this.field == null && this.tokenType == null;
    }

    public boolean isValid() {

        if (this.child != null) {
            return this.isEmpty();
        }

        if (this.getField() == null || this.getTokenType() == null || this.isString != this.isStringClosed) {
            return false;
        }

        switch (this.getTokenType()) {
            //case BETWEEN_OPERATOR: return this.getValueA() != null && this.getValueB() != null;
            case ISNULL_OPERATOR: return this.getValueA() == null;
            default: return this.getValueA() != null;
        }

    }

    public boolean validate() {
        if (this.parent != null) {
            return false;
        } else if (this.left != null) {
            return this.left.validate();
        } else {
            return this.doValidate();
        }
    }

    private boolean doValidate() {
        return this.isValid()
                && (this.child == null || this.child.isValid())
                && (this.and == null || this.and.isValid())
                && (this.or == null || this.or.isValid());
    }

    public boolean isExpectingField() {
        return this.getField() == null;
    }

    public boolean isExpectingOperator() {
        return this.getField() != null && this.tokenType == null;
    }

    public boolean isExpectactingValue() {
        return this.getField() != null && this.getTokenType() != null && this.getValueA() == null;
    }

    public boolean isExepectingClosingQuote() {
        return this.getField() != null && this.getTokenType() != null && this.isString();
    }
}
