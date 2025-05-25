package br.ifsp.film_catalog.model;

import java.util.HashSet;
import java.util.Set;

import br.ifsp.film_catalog.model.key.UserMovieId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id // The Primary Key for Review will be the UserMovieId from UserWatched
    private UserMovieId id;

    /**
     * This defines the One-to-One relationship.
     * @MapsId tells JPA to use the primary key of the associated UserWatched entity
     * as the primary key for this Review entity.
     * This makes a Review's existence entirely dependent on a UserWatched record.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumns({
        @JoinColumn(name="user_id", referencedColumnName="user_id"),
        @JoinColumn(name="movie_id", referencedColumnName="movie_id")
    })
    private UserWatched userWatched;

    @Column(nullable = false)
    private boolean hidden = false;

    // Renamed from 'userComments' for clarity, represents the text of the review.
    @Column(columnDefinition = "TEXT")
    private String content;

    private int directionScore;
    private int screenplayScore;
    private int cinematographyScore;
    private int generalScore;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @OneToMany(
        mappedBy = "review",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<ContentFlag> flags = new HashSet<>();

    public Review(UserWatched userWatched, String content) {
        this.userWatched = userWatched;
        this.id = userWatched.getId(); // Set the ID from the parent UserWatched
        this.content = content;
    }

    public Long getMovieId() {
        return this.id.getMovieId();
    }
}
