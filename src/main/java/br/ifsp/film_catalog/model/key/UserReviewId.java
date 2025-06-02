package br.ifsp.film_catalog.model.key;

import lombok.*;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserReviewId implements Serializable {
    @Column(name = "reporter_user_id")
    private Long userId;
    
    @Column(name = "review_id")
    private Long reviewId;
}