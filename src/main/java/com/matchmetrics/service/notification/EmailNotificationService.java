package com.matchmetrics.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    @Value("${app.resend.api-key:}")
    private String apiKey;

    @Value("${app.from-email:MatchMetrics <noreply@example.com>}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String toEmail, String userName, String code) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[EMAIL-STUB] RESEND_API_KEY not configured — code for {}: {}", toEmail, code);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(toEmail),
                    "subject", "Tu código de recuperación — MatchMetrics",
                    "html", buildHtml(userName, code)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(RESEND_URL, request, String.class);
            log.info("[EMAIL] Code sent to {}", toEmail);

        } catch (Exception e) {
            log.error("[EMAIL] Failed to send code to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildHtml(String userName, String code) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                  <h2 style="color: #0D47A1; margin-bottom: 8px;">MatchMetrics</h2>
                  <p style="color: #374151;">Hola <strong>%s</strong>,</p>
                  <p style="color: #374151;">Tu código para cambiar la contraseña es:</p>
                  <div style="background: #F3F4F6; border-radius: 8px; padding: 24px; text-align: center; margin: 24px 0;">
                    <span style="font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #0D47A1;">%s</span>
                  </div>
                  <p style="color: #6B7280; font-size: 14px;">Este código caduca en <strong>15 minutos</strong>.</p>
                  <p style="color: #6B7280; font-size: 14px;">Si no solicitaste este cambio, ignora este mensaje.</p>
                  <hr style="border: none; border-top: 1px solid #E5E7EB; margin: 24px 0;" />
                  <p style="color: #9CA3AF; font-size: 12px;">MatchMetrics — La plataforma deportiva de tu equipo</p>
                </div>
                """.formatted(userName, code);
    }
}
