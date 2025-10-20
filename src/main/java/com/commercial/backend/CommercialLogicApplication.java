package com.commercial.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = {"com.commercial"})
@SpringBootApplication
public class CommercialLogicApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommercialLogicApplication.class, args);
	}

}
