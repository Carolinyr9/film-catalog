package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.ifsp.film_catalog.model.key.UserReviewId;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "review"})
@Table(name = "content_flags")
public class ContentFlag {

    @EmbeddedId
    private UserReviewId id;

    @Setter
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Setter
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "flag_reason", nullable = false)
    private String flagReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "reporter_user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reviewId")
    @JoinColumn(name = "review_id", referencedColumnName = "id")
    private Review review;

    public ContentFlag(User user, Review review, String flagReason) {
        this.user = user;
        this.review = review;
        this.id = new UserReviewId(user.getId(), review.getId());
        this.flagReason = flagReason;
    }
}
