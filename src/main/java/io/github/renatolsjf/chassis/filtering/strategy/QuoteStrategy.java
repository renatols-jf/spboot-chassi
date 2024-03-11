package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class QuoteStrategy implements TokenTypeStrategy {
    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.isExpectactingValue()) {
            statement.setString(true);
            return statement;
        } else if (statement.isExepectingClosingQuote()) {
            statement.setStringClosed(true);
            return statement;
        } else {
            throw new InvalidSyntaxException("Invalid syntax near: " + token.toString());
        }
    }
}
