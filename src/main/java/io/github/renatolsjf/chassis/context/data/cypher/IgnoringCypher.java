package io.github.renatolsjf.chassis.context.data.cypher;

public class IgnoringCypher implements ClassifiedCypher {

    @Override
    public String encrypt(Object value) {
        return null;
    }

}
