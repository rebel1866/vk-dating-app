package com.melnikov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VkDateAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VkDateAppApplication.class, args);
	}

}
