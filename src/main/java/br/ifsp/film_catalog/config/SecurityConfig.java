package br.ifsp.film_catalog.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import br.ifsp.film_catalog.security.CustomJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
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
                    // AuthenticationController
                    .requestMatchers("/api/auth/**").permitAll() //

                    // UserController
                    .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll() //
                    .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/", "/api/users/search/by-username").hasRole("ADMIN") // Based on @PreAuthorize("hasRole('ADMIN')") in UserController for getAllUsers and preAuthorize for getUserByUsername with ADMIN role
                    .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/").hasRole("ADMIN") // Based on @PreAuthorize("hasRole('ADMIN')") in UserController for createUserByAdmin
                    .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasRole("ADMIN") // Based on @PreAuthorize("hasRole('ADMIN')") in UserController for updateUser
                    .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN") // Based on @PreAuthorize("hasRole('ADMIN')") in UserController for deleteUser
                    .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasAnyRole("ADMIN", "USER") // @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)") - simplified to authenticated user for now, specific ownership check is done by @PreAuthorize
                    .requestMatchers(HttpMethod.PATCH, "/api/users/{id}").hasAnyRole("ADMIN", "USER") // @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)") - simplified
                    .requestMatchers("/api/users/{userId}/favorites/**").hasAnyRole("ADMIN", "USER") // @PreAuthorize for add/remove/get favorite movies
                    .requestMatchers("/api/users/{userId}/watched/**").hasAnyRole("ADMIN", "USER") // @PreAuthorize for add/remove/get watched movies
                    .requestMatchers("/api/users/{userId}/recommendations").authenticated() // Authenticated users can get recommendations


                    // MovieController - Admin-only operations based on @PreAuthorize("hasRole('ADMIN')")
                    .requestMatchers(HttpMethod.POST, "/api/movies", "/api/movies/").hasRole("ADMIN") //
                    .requestMatchers(HttpMethod.PUT, "/api/movies/{id}").hasRole("ADMIN") // (updateMovie - hasRole('ADMIN') commented out but implied)
                    .requestMatchers(HttpMethod.PATCH, "/api/movies/{id}").hasRole("ADMIN") // (patchMovie - hasRole('ADMIN') commented out but implied)
                    .requestMatchers(HttpMethod.DELETE, "/api/movies/{id}").hasRole("ADMIN") // (deleteMovie - hasRole('ADMIN') commented out but implied)
                    // MovieController - Public/Authenticated operations
                    .requestMatchers(HttpMethod.GET, "/api/movies", "/api/movies/**", "/api/movies/search/**", "/api/movies/highlighted").authenticated() // Generally, movie listing and searching should be for authenticated users.


                    // GenreController - Admin-only for CUD based on commented @PreAuthorize and typical API design
                    .requestMatchers(HttpMethod.POST, "/api/genres", "/api/genres/").hasRole("ADMIN") // (createGenre - hasRole('ADMIN') commented out but implied)
                    .requestMatchers(HttpMethod.PUT, "/api/genres/{id}").hasRole("ADMIN") // (updateGenre - hasRole('ADMIN') commented out but implied)
                    .requestMatchers(HttpMethod.DELETE, "/api/genres/{id}").hasRole("ADMIN") // (deleteGenre - implied Admin)
                    // GenreController - Public/Authenticated for read operations
                    .requestMatchers(HttpMethod.GET, "/api/genres", "/api/genres/**").authenticated() // (getAllGenres, getGenreById - should be for authenticated users)


                    // ReviewController - Endpoints have specific @PreAuthorize checks, so a general 'authenticated' rule is a good base.
                    // Individual @PreAuthorize will further restrict.
                    .requestMatchers("/api/users/{userId}/movies/{movieId}/reviews").hasRole("USER") // createReview
                    .requestMatchers(HttpMethod.GET, "/api/reviews/{reviewId}").authenticated() // getReviewById
                    .requestMatchers(HttpMethod.GET, "/api/movies/{movieId}/reviews").authenticated() // getReviewsByMovie
                    .requestMatchers(HttpMethod.GET, "/api/users/{userId}/reviews").authenticated() // getReviewsByUser, @PreAuthorize handles specific user/admin
                    .requestMatchers(HttpMethod.PUT, "/api/reviews/{reviewId}").authenticated() // updateReview, @PreAuthorize handles owner
                    .requestMatchers(HttpMethod.DELETE, "/api/reviews/{reviewId}").authenticated() // deleteReview, @PreAuthorize handles owner
                    .requestMatchers(HttpMethod.POST, "/api/reviews/{reviewId}/like").authenticated() // likeReview
                    .requestMatchers(HttpMethod.POST, "/api/reviews/{reviewId}/flag").authenticated() // flagReview
                    .requestMatchers(HttpMethod.GET, "/api/export/pdf").hasRole("ADMIN") // exportAsPdf
                    .requestMatchers(HttpMethod.GET, "/api/reviews/{userId}/userStatistics").hasRole("ADMIN") // getUserStatistics
                    .requestMatchers(HttpMethod.GET, "/api/reviews/{userId}/average-weighted").hasRole("ADMIN") // getAverageWeighted


                    // ModerationController - All endpoints require ADMIN role based on class-level @PreAuthorize
                    .requestMatchers("/api/moderation/**").hasRole("ADMIN") //


                    // WatchlistController - Endpoints have specific @PreAuthorize checks.
                    .requestMatchers("/api/users/{userId}/watchlists/**").authenticated() // All watchlist operations require authentication, further checks by @PreAuthorize.
                    .anyRequest().authenticated() // Default for any other unlisted endpoint
                )
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