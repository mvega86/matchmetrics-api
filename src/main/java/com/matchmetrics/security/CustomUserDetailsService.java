package com.matchmetrics.security;

import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new UserPrincipal(user);
    }
}