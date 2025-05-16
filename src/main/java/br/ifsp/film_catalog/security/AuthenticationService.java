package br.ifsp.film_catalog.security;

import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthenticationService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public String authenticate(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(user);
    }
}
