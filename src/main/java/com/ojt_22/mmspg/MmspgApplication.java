package com.ojt_22.mmspg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MmspgApplication {

	public static void main(String[] args) {
		SpringApplication.run(MmspgApplication.class, args);
	}

}
