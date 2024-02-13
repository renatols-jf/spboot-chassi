package com.github.renatolsjf.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.github.renatolsjf")
public class ChassiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChassiApplication.class, args);
	}

}
