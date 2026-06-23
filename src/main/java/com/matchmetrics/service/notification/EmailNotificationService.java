package com.matchmetrics.service.notification;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

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

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /** true → stub permitido (local); false → RESEND_API_KEY obligatorio (producción) */
    @Value("${app.email.stub-allowed:true}")
    private boolean stubAllowed;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void validate() {
        if (!stubAllowed && (apiKey == null || apiKey.isBlank())) {
            throw new IllegalStateException(
                "[EMAIL] RESEND_API_KEY es obligatorio cuando EMAIL_STUB_ALLOWED=false. " +
                "Configura la variable de entorno RESEND_API_KEY antes de arrancar en producción.");
        }
    }

    public void send(String toEmail, String userName, String code) {
        if (apiKey == null || apiKey.isBlank()) {
            if (!stubAllowed) {
                throw new RuntimeException(
                    "[EMAIL] RESEND_API_KEY no configurado. El envío de emails no está disponible.");
            }
            // Stub local — visible en cualquier terminal
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║  [EMAIL-STUB] RESEND_API_KEY no configurado         ║");
            System.out.printf ("║  Destinatario : %-36s║%n", maskEmail(toEmail));
            System.out.printf ("║  Usuario      : %-36s║%n", userName);
            System.out.printf ("║  CÓDIGO       : %-36s║%n", code);
            System.out.println("╚══════════════════════════════════════════════════════╝");
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
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_URL, request, String.class);

            log.info("[EMAIL] Código enviado a {} | from: {} | status: {}",
                    maskEmail(toEmail), fromEmail, response.getStatusCode().value());

        } catch (HttpClientErrorException e) {
            log.error("[EMAIL] Error enviando a {} | status: {} | body: {}",
                    maskEmail(toEmail), e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send email. Please try again later.");
        } catch (Exception e) {
            log.error("[EMAIL] Error inesperado enviando a {}: {}", maskEmail(toEmail), e.getMessage());
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildHtml(String userName, String code) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String loginUrl = frontendUrl + "/login";
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
                  <div style="text-align: center; margin: 24px 0;">
                    <a href="%s" style="background: #1565C0; color: white; text-decoration: none;
                       padding: 12px 28px; border-radius: 6px; font-weight: 600; font-size: 14px;">
                      Ir al inicio de sesión
                    </a>
                  </div>
                  <hr style="border: none; border-top: 1px solid #E5E7EB; margin: 24px 0;" />
                  <p style="color: #9CA3AF; font-size: 12px;">MatchMetrics — La plataforma deportiva de tu equipo</p>
                </div>
                """.formatted(safeUserName, code, loginUrl);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(at);
    }
}
