package io.github.renatolsjf.chassis.filtering;

public class Test {

    public static void main(String... args) {
        System.out.println(token("(not endereco.logradouro = 'peste) AND ((endereco.numero isnull)' OR (endereco.numero > 50))"));
    }

    public static Token token(String s) {
        return new Tokenizer().tokenize(s);
    }

}
