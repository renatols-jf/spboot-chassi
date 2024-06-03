package io.github.renatolsjf.chassis.util.expression;

import io.github.renatolsjf.chassis.context.Context;

public class ContextExpression implements Expression {
    @Override
    public Object value() {
        if (Context.isAvailable()) {
            return Context.forRequest();
        } else {
            return null;
        }
    }
}
