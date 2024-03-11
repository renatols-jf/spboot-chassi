package io.github.renatolsjf.chassis.filtering.strategy;

import io.github.renatolsjf.chassis.filtering.InvalidSyntaxException;
import io.github.renatolsjf.chassis.filtering.Statement;
import io.github.renatolsjf.chassis.filtering.Token;

public interface TokenTypeStrategy {

    Statement crateStatement(Token token, Statement statement) throws InvalidSyntaxException;

}
