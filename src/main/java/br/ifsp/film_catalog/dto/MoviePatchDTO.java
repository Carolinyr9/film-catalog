package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class MoviePatchDTO {
    @Size(max = 255, message = "Movie title cannot exceed 255 characters.")
    private String title;

    @Size(max = 255, message = "Synopsis cannot exceed 255 characters.")
    private String synopsis;

    @Positive(message = "Release year must be a positive number.")
    private Integer releaseYear;

    @Positive(message = "Duration must be a positive number.")
    private Integer duration;

    @Pattern(regexp = "AL|A10|A12|A14|A16|A18|OTHER", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid content rating value. Must be one of: AL, A10, A12, A14, A16, A18, OTHER.")
    private String contentRating;

    private Set<Long> genreIds;
}
