package com.matchmetrics.service.notification;

public interface INotificationService {
    void sendByEmail(String email, String userName, String code);
    void sendBySms(String phone, String userName, String code);
}
