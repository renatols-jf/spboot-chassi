package io.github.renatolsjf.chassis.filtering;

import java.util.List;

public class Test {

    public static void main(String... args) throws InvalidSyntaxException {
        Token t = token("(not endereco.logradouro = 'peste) AND ((endereco.numero isnull) OR (endereco.numero = 50)'))");
        System.out.println(t);
        Statement s = t.createStatement();
        System.out.println(s);
        System.out.println(s.validate());
    }

    public static Token token(String s) {
        return new Tokenizer().tokenize(s);
    }

    public static List<Token> list(String s) {
        return token(s).dismember();
    }

}
