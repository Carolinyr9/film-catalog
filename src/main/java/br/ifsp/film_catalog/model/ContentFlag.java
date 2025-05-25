package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import br.ifsp.film_catalog.model.key.UserReviewId;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "content_flags")
public class ContentFlag {
    @EmbeddedId
    private UserReviewId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reviewId")
    private Review review;

    @Column(name = "flagged_at", nullable = false)
    private LocalDateTime flaggedAt;

    @Column(name = "flag_reason", nullable = false)
    private String flagReason;

    public ContentFlag(User user, Review review, String flagReason) {
        this.user = user;
        this.review = review;
        this.id = new UserReviewId(user.getId(), review.getMovieId());
        this.flagReason = flagReason;
        this.flaggedAt = LocalDateTime.now();
    }
}
