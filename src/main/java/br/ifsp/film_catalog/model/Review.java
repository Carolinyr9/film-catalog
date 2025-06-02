package br.ifsp.film_catalog.model;

import java.util.HashSet;
import java.util.Set;

import br.ifsp.film_catalog.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Setter
    @Column(nullable = false)
    private boolean hidden = false;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String content;

    @Setter private int directionScore;
    @Setter private int screenplayScore;
    @Setter private int cinematographyScore;
    @Setter private int generalScore;

    @Setter
    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({ // Used because UserWatched has a composite key
        @JoinColumn(name = "user_watched_user_id", referencedColumnName = "user_id"),
        @JoinColumn(name = "user_watched_movie_id", referencedColumnName = "movie_id")
    })
    private UserWatched userWatched;

    @OneToMany(
        mappedBy = "review", // This 'review' is the field in ContentFlag linking back to the flagged Review
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<ContentFlag> flags = new HashSet<>();


    public Review(UserWatched userWatched, String content) {
        this.userWatched = userWatched;
        this.content = content;
    }
}
