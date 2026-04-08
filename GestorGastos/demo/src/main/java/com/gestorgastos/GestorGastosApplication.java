package com.gestorgastos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GestorGastosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestorGastosApplication.class, args);
	}

}
