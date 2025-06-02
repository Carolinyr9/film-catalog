package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import br.ifsp.film_catalog.model.key.UserMovieId;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "movie"})
@Table(name = "user_favorites")
public class UserFavorite {
    @EmbeddedId
    private UserMovieId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Maps the 'userId' part of the EmbeddedId
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId") // Maps the 'movieId' part of the EmbeddedId
    private Movie movie;

    @Column(name = "favorited_at", nullable = false)
    private LocalDateTime favoritedAt;

    public UserFavorite(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
        this.id = new UserMovieId(user.getId(), movie.getId()); // Create the composite ID
        this.favoritedAt = LocalDateTime.now();
    }
}
