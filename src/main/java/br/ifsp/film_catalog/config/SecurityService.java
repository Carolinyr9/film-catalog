package br.ifsp.film_catalog.config;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    public boolean isOwner(Authentication authentication, String username) {
        // Aqui você pode pegar o username do authentication e comparar
        return authentication != null &&
               authentication.getName().equals(username);
    }
}
