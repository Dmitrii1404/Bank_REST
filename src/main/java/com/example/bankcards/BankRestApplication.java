package com.example.bankcards;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankRestApplication {

	static {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue())
			);
		} catch (Exception e) {
			System.err.println("Не получилось загрузить .env " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(BankRestApplication.class, args);
	}

}
