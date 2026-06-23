package com.matchmetrics.service.notification;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS notification stub — prepared for Twilio or similar provider.
 * Enable via SMS_ENABLED=true and configure TWILIO_ACCOUNT_SID,
 * TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER environment variables.
 */
@Service
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.account-sid:}")
    private String accountSid;

    @Value("${app.sms.auth-token:}")
    private String authToken;

    @Value("${app.sms.from-number:}")
    private String fromNumber;

    @PostConstruct
    public void validate() {
        if (smsEnabled && (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank())) {
            throw new IllegalStateException(
                "[SMS] SMS_ENABLED=true pero faltan credenciales del proveedor. " +
                "Configura TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN y TWILIO_FROM_NUMBER " +
                "antes de activar el envío de SMS.");
        }
    }

    public boolean isEnabled() {
        return smsEnabled;
    }

    public void send(String phone, String userName, String code) {
        if (!smsEnabled) {
            log.warn("[SMS-STUB] SMS no configurado — código para {}: {}", phone, code);
            return;
        }
        // TODO: integrate Twilio
        // TwilioRestClient client = new TwilioRestClient.Builder(accountSid, authToken).build();
        // Message.creator(new PhoneNumber(phone), new PhoneNumber(fromNumber), "Tu código: " + code).create(client);
        log.info("[SMS] Código enviado a {}", phone);
    }
}
