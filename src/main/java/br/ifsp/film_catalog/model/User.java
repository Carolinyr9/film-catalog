package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import br.ifsp.film_catalog.model.common.BaseEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(unique = true)
    private String email;

    @Setter
    @Column(name = "password")
    private String password;

    @Setter
    @Column(unique = true)
    private String username;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Watchlist> watchlists = new HashSet<>();

    public void addWatchlist(Watchlist watchlist) {
        this.watchlists.add(watchlist);
        watchlist.setUser(this); // Set the back-reference
    }

    public void removeWatchlist(Watchlist watchlist) {
        this.watchlists.remove(watchlist);
        watchlist.setUser(null); // Remove the back-reference
    }

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<UserFavorite> favoriteMovies = new HashSet<>();

    public void addFavorite(Movie movie) {
        UserFavorite favorite = new UserFavorite(this, movie);
        this.favoriteMovies.add(favorite);
        movie.getFavoritedBy().add(favorite); // Keep both sides in sync
    }

    public void removeFavorite(Movie movie) {
        // Create an iterator to safely remove while iterating
        this.favoriteMovies.removeIf(favorite ->
                favorite.getUser().equals(this) &&
                favorite.getMovie().equals(movie)
        );
        movie.getFavoritedBy().removeIf(favorite ->
                favorite.getUser().equals(this) &&
                favorite.getMovie().equals(movie)
        );
    }

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<UserWatched> watchedMovies = new HashSet<>();

    public void addWatched(Movie movie, LocalDateTime watchedAt) {
        UserWatched watched = new UserWatched(this, movie, watchedAt);
        this.watchedMovies.add(watched);
        movie.getWatchedBy().add(watched);
    }

    public void removeWatched(Movie movie) {
        this.watchedMovies.removeIf(watched ->
                watched.getUser().equals(this) &&
                watched.getMovie().equals(movie)
        );
        movie.getFavoritedBy().removeIf(watched ->
                watched.getUser().equals(this) &&
                watched.getMovie().equals(movie)
        );
    }

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ContentFlag> flaggedContent = new HashSet<>();

    public void addFlaggedContent(Review review, String flagReason) {
        ContentFlag flag = new ContentFlag(this, review, flagReason);
        this.flaggedContent.add(flag);
        if (review.getFlags() != null) { // Defensive check
            review.getFlags().add(flag);
        }
    }

    public void removeFlaggedContent(Review review) {
        // Iterate to find the specific flag by this user for this review
        ContentFlag toRemove = null;
        for (ContentFlag flag : this.flaggedContent) {
            if (flag.getReview().equals(review) && flag.getUser().equals(this)) {
                toRemove = flag;
                break;
            }
        }
        if (toRemove != null) {
            this.flaggedContent.remove(toRemove);
            if (review.getFlags() != null) { // Defensive check
                review.getFlags().remove(toRemove);
            }
        }
    }

}
