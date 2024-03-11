package io.github.renatolsjf.chassis.filtering;

import java.util.List;

public class Test {

    public static void main(String... args) {
        System.out.println(token("(not endereco.logradouro = 'peste) AND ((endereco.numero isnull)' OR (endereco.numero er 50 and 100))"));
    }

    public static Token token(String s) {
        return new Tokenizer().tokenize(s);
    }

    public static List<Token> list(String s) {
        return token(s).dismember();
    }

}
