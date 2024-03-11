package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class IdentifierStrategy implements TokenTypeStrategy {
    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.isExpectingField()) {
            statement.setField(token.getIdentifier());
            return statement;
        } else if (statement.isExpectactingValue()) {
            statement.setValueA(token.getIdentifier());
            return statement;
        } else {
            throw new InvalidSyntaxException("Invalid syntax near: " + token.toString());
        }
    }
}
