package com.github.renatolsjf.chassi.context.data.cypher;

public class IgnoringCypher implements ClassifiedCypher {

    @Override
    public String encrypt(Object value) {
        return null;
    }

}
