package com.matchmetrics.mapper.dto.auth;

import java.util.List;

public record ForgotPasswordMethodsResponse(
        List<String> availableMethods,
        String maskedEmail,
        String maskedPhone
) {}
