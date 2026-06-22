package com.matchmetrics.service.notification;

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

    public boolean isEnabled() {
        return smsEnabled;
    }

    public void send(String phone, String userName, String code) {
        if (!smsEnabled) {
            log.warn("[SMS-STUB] SMS not configured — code for {}: {}", phone, code);
            return;
        }
        // TODO: integrate Twilio or another SMS provider
        // TwilioRestClient client = new TwilioRestClient.Builder(accountSid, authToken).build();
        // Message.creator(new PhoneNumber(phone), new PhoneNumber(fromNumber), "Tu código: " + code).create(client);
        log.info("[SMS] Code sent to {}", phone);
    }
}
