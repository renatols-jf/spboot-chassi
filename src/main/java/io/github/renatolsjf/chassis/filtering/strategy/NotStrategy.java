package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class NotStrategy implements TokenTypeStrategy {

    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.getField() != null) {
            throw new InvalidSyntaxException("NOT operator must be at the beginning of a statement: " + token);
        }
        statement.setNegative(true);
        return statement;
    }

}
