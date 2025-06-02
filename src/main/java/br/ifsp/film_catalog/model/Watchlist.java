package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import br.ifsp.film_catalog.model.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watchlists")
public class Watchlist extends BaseEntity {
    @Setter
    private String name = "Watchlist";

    @Setter
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", // This is the foreign key column in the 'watchlists' table.
            nullable = false, // This makes the relationship MANDATORY. A watchlist cannot exist without a user.
            updatable = false // The owner of a watchlist should not change after creation.
    )
    private User user;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "watchlist_movies", // Name of the join table
            joinColumns = @JoinColumn(name = "watchlist_id"), // Column for this entity's ID
            inverseJoinColumns = @JoinColumn(name = "movie_id") // Column for the other entity's ID
    )
    private Set<Movie> movies = new HashSet<>();

    public void addMovie(Movie movie) {
        this.movies.add(movie);
        movie.getWatchlists().add(this); // Keep both sides in sync
    }

    public void removeMovie(Movie movie) {
        this.movies.remove(movie);
        movie.getWatchlists().remove(this); // Keep both sides in sync
    }

    public Watchlist(String name, User user) {
        this.name = name;
        this.user = user;
    }
    
}
