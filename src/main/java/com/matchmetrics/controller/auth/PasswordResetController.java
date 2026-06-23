package com.matchmetrics.controller.auth;

import com.matchmetrics.mapper.dto.auth.ForgotPasswordMethodsResponse;
import com.matchmetrics.mapper.dto.auth.GetAvailableMethodsRequest;
import com.matchmetrics.mapper.dto.auth.ResetPasswordRequest;
import com.matchmetrics.mapper.dto.auth.SendResetCodeRequest;
import com.matchmetrics.service.IPasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/forgot-password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final IPasswordResetService passwordResetService;

    @PostMapping("/methods")
    public ForgotPasswordMethodsResponse getMethods(@Valid @RequestBody GetAvailableMethodsRequest request) {
        return passwordResetService.getAvailableMethods(request.getEmail());
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendCode(@Valid @RequestBody SendResetCodeRequest request) {
        passwordResetService.sendResetCode(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
