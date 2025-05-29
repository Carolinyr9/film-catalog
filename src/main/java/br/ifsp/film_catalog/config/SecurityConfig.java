package br.ifsp.film_catalog.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import br.ifsp.film_catalog.security.CustomJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${jwt.public.key}")
    private RSAPublicKey key;
    @Value("${jwt.private.key}")
    private RSAPrivateKey priv;
    
    @Bean
    public CustomJwtAuthenticationConverter customJwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            CustomJwtAuthenticationConverter customJwtAuthenticationConverter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN") 

                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/{userId}/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{userId}/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}/watched/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/{userId}/watched/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{userId}/watched/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()

                        .requestMatchers("/api/movies/**").permitAll() 
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN") 
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN") 
                        .requestMatchers(HttpMethod.PATCH, "/api/movies/**").hasRole("ADMIN") 
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN") 
                        
                        .requestMatchers("/api/genres/**").permitAll() 
                        .requestMatchers(HttpMethod.POST, "/api/genres").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/genres/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/genres/**").hasRole("ADMIN")
                        
                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        conf -> conf.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }
    
    @Bean
    JwtEncoder jwtEncoder() {
        var jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
}