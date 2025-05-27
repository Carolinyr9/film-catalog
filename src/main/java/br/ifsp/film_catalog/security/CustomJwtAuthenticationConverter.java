// CustomJwtAuthenticationConverter.java
package br.ifsp.film_catalog.security;

import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        UserAuthenticated userAuthenticated = extractUser(jwt);
        List<GrantedAuthority> authorities = List.copyOf(userAuthenticated.getAuthorities());
        return new UsernamePasswordAuthenticationToken(userAuthenticated, null, authorities);
    }

    private UserAuthenticated extractUser(Jwt jwt) {
        Object userIdObj = jwt.getClaims().get("userId");
        Long userId;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            userId = Long.valueOf((String) userIdObj);
        } else {
            throw new IllegalArgumentException("Invalid userId claim type");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return new UserAuthenticated(user);
    }
}
