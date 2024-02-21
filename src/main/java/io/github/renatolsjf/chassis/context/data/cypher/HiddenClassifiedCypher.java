package io.github.renatolsjf.chassis.context.data.cypher;

public class HiddenClassifiedCypher implements ClassifiedCypher {
    @Override
    public String encrypt(Object value) {
        return value != null
                ? "Classified; informed"
                : "Classified; NOT informed";
    }
}
