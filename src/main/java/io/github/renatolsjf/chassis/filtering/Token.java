package io.github.renatolsjf.chassis.filtering;

import java.util.ArrayList;
import java.util.List;

public class Token {

    protected Token prior;
    protected TokenType tokenType;
    protected String identifier;

    public enum TokenType {
        NOT_OPERATOR("NOT", false),
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
        CLOSE_PARENTHESIS_SEPARATOR(")", true),
        QUOTE_SEPARATOR("'", true);

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
                    ? "ID: " + this.identifier + " ENDID"
                    : this.tokenType.toString());

    }

    public Statement createStatement() {
        Statement statement = new Statement();
        List<Token> tokenList = this.dismember();
        for(Token t: tokenList) {

        }
    }

    public List<Token> dismember() {
        List<Token> tokenList = new ArrayList<>();
        this.dismember(tokenList);
        return tokenList;
    }

    private void dismember(List<Token> tokenList) {
        if (this.prior != null) {
            this.prior.dismember(tokenList);
        }
        tokenList.add(this);
    }

}
