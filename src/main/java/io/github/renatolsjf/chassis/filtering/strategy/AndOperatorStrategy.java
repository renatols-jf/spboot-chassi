package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public class AndOperatorStrategy implements TokenTypeStrategy {
    @Override
    public Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException {
        if (Token.TokenType.BETWEEN_OPERATOR.equals(statement.getTokenType()) && statement.getValueA() != null && statement.getValueB() == null) {
            return statement;
        } else if (statement.getValueA() == null) {
            throw new InvalidSyntaxException("No value found before AND: " + token.toString());
        } else  {
            Statement newStatement = new Statement();
            statement.setAnd(newStatement);
        }
    }
}
