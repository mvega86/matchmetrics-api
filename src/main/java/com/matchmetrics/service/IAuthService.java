package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.auth.AuthMeResponse;
import com.matchmetrics.mapper.dto.auth.AuthResponse;
import com.matchmetrics.mapper.dto.auth.ChangePasswordRequest;
import com.matchmetrics.mapper.dto.auth.LoginRequest;
import com.matchmetrics.mapper.dto.auth.RegisterRequest;
import com.matchmetrics.mapper.dto.auth.UpdateProfileRequest;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    AuthMeResponse getProfile(Long userId);

    AuthMeResponse updateProfile(Long userId, UpdateProfileRequest request);
}