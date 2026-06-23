package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.auth.ForgotPasswordMethodsResponse;
import com.matchmetrics.mapper.dto.auth.ResetPasswordRequest;
import com.matchmetrics.mapper.dto.auth.SendResetCodeRequest;

public interface IPasswordResetService {
    ForgotPasswordMethodsResponse getAvailableMethods(String email);
    void sendResetCode(SendResetCodeRequest request);
    void resetPassword(ResetPasswordRequest request);
}
