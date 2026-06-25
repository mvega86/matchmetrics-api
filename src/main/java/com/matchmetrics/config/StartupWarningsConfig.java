package com.matchmetrics.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupWarningsConfig {

    private static final Logger log = LoggerFactory.getLogger(StartupWarningsConfig.class);

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.from-email:MatchMetrics <noreply@example.com>}")
    private String fromEmail;

    @PostConstruct
    public void warnOnLocalFallbacks() {
        if (baseUrl.contains("localhost")) {
            log.warn("[CONFIG] APP_BASE_URL usa el valor local '{}'. " +
                     "Configura APP_BASE_URL con la URL pública del backend en producción.", baseUrl);
        }
        if (frontendUrl.contains("localhost")) {
            log.warn("[CONFIG] APP_FRONTEND_URL usa el valor local '{}'. " +
                     "Configura APP_FRONTEND_URL con la URL pública del frontend en producción.", frontendUrl);
        }
        if (fromEmail.contains("noreply@example.com")) {
            log.warn("[CONFIG] APP_FROM_EMAIL usa el valor por defecto '{}'. " +
                     "Configura APP_FROM_EMAIL con la dirección verificada en Resend en producción.", fromEmail);
        }
    }
}
