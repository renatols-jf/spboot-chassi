package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class CloseStrategy implements TokenTypeStrategy {

    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (statement.isValid()) {
            return statement.getParent();
        } else {
            throw new InvalidSyntaxException("Invalid parentheses location: " + token.toString());
        }
    }

}
