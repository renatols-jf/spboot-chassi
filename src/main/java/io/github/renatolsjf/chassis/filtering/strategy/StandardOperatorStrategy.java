package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class StandardOperatorStrategy implements TokenTypeStrategy {
    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.isExpectingOperator()) {
            statement.setTokenType(token.getTokenType());
            return statement;
        } else {
            throw new InvalidSyntaxException("Invalid syntax near: " + token.toString());
        }
    }
}
