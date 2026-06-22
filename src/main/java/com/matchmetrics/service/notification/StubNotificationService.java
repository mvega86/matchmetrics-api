package com.matchmetrics.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StubNotificationService implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(StubNotificationService.class);

    @Override
    public void sendByEmail(String email, String userName, String code) {
        log.info("[STUB-EMAIL] To: {} | User: {} | Code: {}", email, userName, code);
    }

    @Override
    public void sendBySms(String phone, String userName, String code) {
        log.info("[STUB-SMS] To: {} | User: {} | Code: {}", phone, userName, code);
    }
}
