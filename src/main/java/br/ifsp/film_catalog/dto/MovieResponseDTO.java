package br.ifsp.film_catalog.dto;

import br.ifsp.film_catalog.model.enums.ContentRating;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;
import lombok.Builder;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponseDTO {
    private Long id;
    private String title;
    private String synopsis;
    private int releaseYear;
    private int duration;
    private ContentRating contentRating;
    private Set<GenreResponseDTO> genres = new HashSet<>();
}