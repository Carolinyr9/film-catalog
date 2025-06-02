package br.ifsp.film_catalog.model.key;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Represents the composite primary key for relationships between a User and a Movie.
 * This class is embeddable in other entities.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserMovieId implements Serializable {
    private Long userId;
    private Long movieId;
}
