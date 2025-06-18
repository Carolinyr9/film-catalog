package br.ifsp.film_catalog.config;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    public boolean isOwner(Authentication authentication, String username) {
        return authentication != null &&
               authentication.getName().equals(username);
    }

    public boolean isReviewOwner(Authentication authentication, Long reviewId) {
    // Simplesmente retorna true para teste
    return true;
}

}
