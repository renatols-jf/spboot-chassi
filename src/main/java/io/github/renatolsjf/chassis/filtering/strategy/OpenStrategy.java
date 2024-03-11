package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class OpenStrategy implements TokenTypeStrategy {
    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.isEmpty()) {
            Statement newStatement = new Statement();
            statement.setChild(newStatement);
            return newStatement;
        } else {
            throw new InvalidSyntaxException("Invalid parentheses location: " + token.toString());
        }
    }
}
