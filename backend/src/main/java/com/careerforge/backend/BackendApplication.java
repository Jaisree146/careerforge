package com.careerforge.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class BackendApplication {

	public static void main(String[] args) {
		// Load .env variables into System properties if not already set by the environment
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(BackendApplication.class, args);
	}
}
