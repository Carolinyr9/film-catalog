package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import br.ifsp.film_catalog.model.common.BaseEntity;
import br.ifsp.film_catalog.model.enums.ContentRating;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "movies")
public class Movie extends BaseEntity {

    @Setter
    @Column(nullable = false, unique = true)
    private String title;   
    
    @Setter
    private String synopsis;

    @Setter
    private int releaseYear;

    @Setter
    private int duration;

    @Setter
    private ContentRating contentRating; // Classificação indicativa

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(
            name = "movie_genres", // Name of the new join table
            joinColumns = @JoinColumn(name = "movie_id"), // Column in join table for this entity's ID
            inverseJoinColumns = @JoinColumn(name = "genre_id") // Column in join table for the other entity's ID
    )
    private Set<Genre> genres = new HashSet<>();

    /**
     * Safely adds a Genre to this Movie, managing both sides of the relationship.
     * @param genre The Genre to add.
     */
    public void addGenre(Genre genre) {
        this.genres.add(genre);
        genre.getMovies().add(this);
    }

    /**
     * Safely removes a Genre from this Movie, managing both sides of the relationship.
     * @param genre The Genre to remove.
     */
    public void removeGenre(Genre genre) {
        this.genres.remove(genre);
        genre.getMovies().remove(this);
    }

    @ManyToMany(mappedBy = "movies")
    private Set<Watchlist> watchlists = new HashSet<>();

    @OneToMany(
        mappedBy = "movie",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<UserFavorite> favoritedBy = new HashSet<>();

    @OneToMany(
        mappedBy = "movie",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<UserWatched> watchedBy = new HashSet<>();

}
