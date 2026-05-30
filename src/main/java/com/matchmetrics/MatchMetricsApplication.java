package com.matchmetrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication(scanBasePackages = "com.matchmetrics")
public class MatchMetricsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchMetricsApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(@Autowired DataSource dataSource) {
		return args -> {
			System.out.println("Conexión a la base de datos establecida: " + dataSource.getConnection().getMetaData().getURL());
		};
	}

}
