package io.github.renatolsjf.chassis.filtering;

public class Tokenizer {

    private StringBuffer tokenBuffer = new StringBuffer();
    private Token token;
    private boolean quoteMode = false;

    public Token tokenize(String content) {

        content = content.trim();

        for (int i = 0; i < content.length(); i++) {

            char c = content.charAt(i);

            if (!quoteMode) {
                if (!Character.isWhitespace(c)) {
                    tokenBuffer.append(c);
                    this.update(tokenBuffer);
                }  else if (!tokenBuffer.isEmpty()) {
                    Token.TokenType tt = Token.TokenType.fromString(tokenBuffer.toString());
                    if (tt != null) {
                        this.createToken(tt);
                    } else {
                        this.createIdentifier(tokenBuffer.toString());
                    }
                    tokenBuffer.setLength(0);
                }
            } else {
                Token.TokenType tt = Token.TokenType.fromString(String.valueOf(c));
                if (tt != null && tt == Token.TokenType.QUOTE_SEPARATOR) {
                    this.createIdentifier(tokenBuffer.substring(0, tokenBuffer.length() - 1));
                    this.createToken(tt);
                    this.quoteMode = false;
                    tokenBuffer.setLength(0);
                } else {
                    tokenBuffer.append(c);
                }
            }


        }

        if (!tokenBuffer.isEmpty()) {
            Token.TokenType tt = Token.TokenType.fromString(tokenBuffer.toString());
            if (tt != null) {
                this.createToken(tt);
            } else {
                this.createIdentifier(tokenBuffer.toString());
            }
        }

        return token;

    }

    private void update (StringBuffer buffer) {
        for (int i = buffer.length() - 1; i >= 0; i--) {
            Token.TokenType tt = Token.TokenType.keywordFromString(tokenBuffer.substring(i, tokenBuffer.length()));
            if (tt != null) {
                if (i != 0) {
                    Token.TokenType another = Token.TokenType.fromString(tokenBuffer.substring(0, i));
                    if (another != null) {
                        this.createToken(another);
                    } else {
                        this.createIdentifier(buffer.substring(0, i));
                    }
                }
                this.createToken(tt);
                this.tokenBuffer.setLength(0);

                if (tt == Token.TokenType.QUOTE_SEPARATOR) {
                    this.quoteMode = true;
                }

                break;
            }
        }
    }

    /*public Token tokenize(String content) {

        content = content.trim();

        Token token = null;
        for (int i = 0; i < content.length(); i++) {

            char c = content.charAt(i);

            if (!Character.isWhitespace(c)) {
                tokenBuffer.append(c);
                Token.TokenType tt = Token.TokenType.fromString(tokenBuffer.toString());
                if (tt != null) {
                    token = createToken(token, tt);
                    tokenBuffer.setLength(0);
                }
            } else if (!tokenBuffer.isEmpty()) {
                token = createIdentifier(token, tokenBuffer.toString());
                tokenBuffer.setLength(0);
            }

        }

        if (!tokenBuffer.isEmpty()) {
            Token.TokenType tt = Token.TokenType.fromString(tokenBuffer.toString());
            if (tt != null) {
                token = createToken(token, tt);
            } else {
                token = createIdentifier(token, tokenBuffer.toString());
            }
        }

        return token;

    }*/

    private void createToken(Token.TokenType tokenType) {
        if (token == null) {
            token = new Token(tokenType);
        } else {
            token = token.next(new Token(tokenType));
        }
    }

    private void createIdentifier(String identifier) {
        if (token == null) {
            token =  new Token(identifier);
        } else {
            token = token.next(new Token(identifier));
        }
    }

    /*public Statement tokenize(String content) throws InvalidFilteringSyntax {

        Statement statement = new Statement();
        content = content.trim();

        for (int i = 0; i < content.length(); i++) {

            char c = content.charAt(i);

            if (Character.isWhitespace(c)) {
                if (phase == Phase.STATEMENT_START && tokenBuffer.length() > 0) {
                    if (tokenBuffer.toString().equalsIgnoreCase(Token.TokenType.NOT_KEYWORD.toString())) {
                        statement
                    }
                    statement.setField(tokenBuffer.toString());
                    tokenBuffer.setLength(0);
                }
            }

            tokenBuffer.append(content.charAt(i));

        }

    }*/

}
