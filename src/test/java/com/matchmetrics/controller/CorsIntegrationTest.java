package com.matchmetrics.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el preflight CORS responde correctamente con Access-Control-Allow-Credentials: true.
 * Este test existe para detectar regresiones cuando se modifica SecurityConfig o WebConfig.
 *
 * Contexto: withCredentials=true en el cliente axios exige que el servidor devuelva
 * Access-Control-Allow-Credentials: true y Access-Control-Allow-Origin con origen concreto.
 * Si falla, el navegador bloquea la respuesta con status null (CORS request did not succeed).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";
    private static final String UNKNOWN_ORIGIN = "https://malicious.example.com";

    @Test
    void preflight_Login_ShouldAllowCredentials_FromKnownOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN));
    }

    @Test
    void preflight_Login_ShouldNotIncludeCorsHeaders_FromUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", UNKNOWN_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void preflight_PublicGet_ShouldAllowCredentials_FromKnownOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/matches/1")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN));
    }
}
