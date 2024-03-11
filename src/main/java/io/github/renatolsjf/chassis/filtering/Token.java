package io.github.renatolsjf.chassis.filtering;

import io.github.renatolsjf.chassis.filtering.strategy.*;

import java.util.ArrayList;
import java.util.List;

public class Token {

    protected Token prior;
    protected TokenType tokenType;
    protected String identifier;

    private static TokenTypeStrategy standardOperatorStrategy = new StandardOperatorStrategy();

    public enum TokenType {
        NOT_OPERATOR("NOT", false, new NotStrategy()),
        AND_OPERATOR("AND", false, new AndStrategy()),
        OR_OPERATOR("OR", false, new OrStrategy()),
        EQUALS_OPERATOR("=", true, standardOperatorStrategy),
        //BETWEEN_OPERATOR("BETWEEN", false),
        ISNULL_OPERATOR("ISNULL", false, standardOperatorStrategy),
        GREATERTHAN_OPERATOR(">", true, standardOperatorStrategy),
        LESSTHAN_OPERATOR("<", true, standardOperatorStrategy),
        //NOOP_OPERATOR("", false),
        //REGEX_CONSTANT,
        IDENTIFIER("", false, new IdentifierStrategy()),
        OPEN_PARENTHESIS_SEPARATOR("(", true, new OpenStrategy()),
        CLOSE_PARENTHESIS_SEPARATOR(")", true, new CloseStrategy()),
        QUOTE_SEPARATOR("'", true, new QuoteStrategy());

        private String stringToken;
        private boolean keyword;
        private TokenTypeStrategy strategy;

        TokenType(String stringToken, boolean keyword, TokenTypeStrategy strategy) {
            this.stringToken = stringToken;
            this.keyword = keyword;
            this.strategy = strategy;
        }

        public TokenTypeStrategy getStrategy() {
            return this.strategy;
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

    public TokenType getTokenType() {
        return this.tokenType;
    }

    public String getIdentifier() {
        return this.identifier;
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

    public Statement createStatement() throws InvalidSyntaxException {
        Statement statement = new Statement();
        List<Token> tokenList = this.dismember();
        for(Token t: tokenList) {
            statement = t.getTokenType().getStrategy().crateStatement(t, statement);
        }
        return statement;
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
