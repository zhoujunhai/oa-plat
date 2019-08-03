package com.ccb.sc.oaplat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class OaPlatApplication {

	public static void main(String[] args) {
		SpringApplication.run(OaPlatApplication.class, args);
	}

}
