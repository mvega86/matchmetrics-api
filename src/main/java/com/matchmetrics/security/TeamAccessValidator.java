package com.matchmetrics.security;

import com.matchmetrics.domain.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TeamAccessValidator {

    public void validateSameTeamOrAdmin(
            UserPrincipal principal,
            Long resourceTeamId
    ) {
        if (principal == null) return;

        if (principal.getRole() == UserRole.ADMIN) {
            return;
        }

        if (principal.getTeamId() == null || resourceTeamId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!principal.getTeamId().equals(resourceTeamId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public void validateAnyTeamOrAdmin(
            UserPrincipal principal,
            Long firstTeamId,
            Long secondTeamId
    ) {
        if (principal == null) return;

        if (principal.getRole() == UserRole.ADMIN) {
            return;
        }

        Long userTeamId = principal.getTeamId();

        if (userTeamId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!userTeamId.equals(firstTeamId)
                && !userTeamId.equals(secondTeamId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
