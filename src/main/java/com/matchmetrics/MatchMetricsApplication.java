package com.matchmetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.matchmetrics")
@EnableScheduling
public class MatchMetricsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchMetricsApplication.class, args);
	}

}
