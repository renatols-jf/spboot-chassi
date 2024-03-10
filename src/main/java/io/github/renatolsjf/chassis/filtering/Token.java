package io.github.renatolsjf.chassis.filtering;

public class Token {

    protected Token prior;
    protected TokenType tokenType;
    protected String identifier;

    protected enum TokenType {
        NOT_OPERATOR("!", true),
        AND_OPERATOR("AND", false),
        OR_OPERATOR("OR", false),
        EQUALS_OPERATOR("=", true),
        BETWEEN_OPERATOR("BETWEEN", false),
        ISNULL_OPERATOR("ISNULL", false),
        GREATERTHAN_OPERATOR(">", true),
        LESSTHAN_OPERATOR("<", true),
        NOOP_OPERATOR("", false),
        //REGEX_CONSTANT,
        //PROPERTY_IDENTIFIER(""),
        //VALUE_IDENTIFIER(""),
        IDENTIFIER("", false),
        OPEN_PARENTHESIS_SEPARATOR("(", true),
        CLOSE_PARENTHESIS_SEPARATOR(")", true);

        private String stringToken;
        private boolean keyword;

        TokenType(String stringToken, boolean keyword) {
            this.stringToken = stringToken;
            this.keyword = keyword;
        }

        public static TokenType fromString(String s) {
            for (TokenType tt:  TokenType.values()) {
                if (!tt.stringToken.isBlank() && tt.stringToken.equalsIgnoreCase(s)) {
                    return tt;
                }
            }
            return null;
        }

        public static TokenType keywordFromString(String s) {
            for (TokenType tt:  TokenType.values()) {
                if (!tt.stringToken.isBlank() && tt.stringToken.equalsIgnoreCase(s) && tt.keyword) {
                    return tt;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.stringToken;
        }
    }

    public Token(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Token(String identifier) {
        this.tokenType = TokenType.IDENTIFIER;
        this.identifier = identifier;
    }

    public Token next(Token anotherToken) {
        anotherToken.prior = this;
        return anotherToken;
    }

    @Override
    public String toString() {
        String s = "";
        if (this.prior != null) {
            s = this.prior.toString() + " ";
        }

        return s + (this.identifier != null
                    ? "ID: " + this.identifier
                    : this.tokenType.toString());

    }

}
