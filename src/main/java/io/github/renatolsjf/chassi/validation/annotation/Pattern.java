package io.github.renatolsjf.chassi.validation.annotation;

public @interface Pattern {

    //String URL = "^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$"; AINDA NÃO ENCONTREI UM REGEX BOM

    String value() default "";
    String message() default "";

}
