package com.matchmetrics.controller.auth;

import com.matchmetrics.mapper.dto.auth.InvitationValidateResponse;
import com.matchmetrics.mapper.dto.auth.SendInvitationRequest;
import com.matchmetrics.service.invitation.UserInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final UserInvitationService invitationService;

    /** Solo ADMIN — enviar invitación por email */
    @PostMapping("/api/v1/admin/invitations")
    public ResponseEntity<Void> sendInvitation(@Valid @RequestBody SendInvitationRequest request) {
        invitationService.sendInvitation(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    /** Público — validar token antes de mostrar el formulario de registro */
    @GetMapping("/api/v1/auth/invitation/{token}")
    public ResponseEntity<InvitationValidateResponse> validateToken(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.validateToken(token));
    }
}
