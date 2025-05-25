package br.ifsp.film_catalog.model.key;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserReviewId implements Serializable {
    private Long userId;
    private Long reviewId;
}
