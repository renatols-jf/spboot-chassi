package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class AndStrategy implements TokenTypeStrategy {
    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.isValid()) {
            Statement newStatement = new Statement();
            statement.setAnd(newStatement);
            return newStatement;
        } else {
            throw new InvalidSyntaxException("Invalid syntax near: " + token.toString());
        }
    }
}
