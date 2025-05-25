package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import br.ifsp.film_catalog.model.key.UserMovieId;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_watcheds")
public class UserWatched {
    @EmbeddedId
    private UserMovieId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Maps the 'userId' part of the EmbeddedId
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId") // Maps the 'movieId' part of the EmbeddedId
    private Movie movie;

    @Column(name = "watched_at", nullable = false)
    private LocalDateTime watchedAt;

    public UserWatched(User user, Movie movie, LocalDateTime watchedAt) {
        this.user = user;
        this.movie = movie;
        this.id = new UserMovieId(user.getId(), movie.getId()); // Create the composite ID
        this.watchedAt = watchedAt;
    }
}
