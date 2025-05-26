package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviePatchDTO {
    @Size(max = 255, message = "Movie title cannot exceed 255 characters.")
    private Optional<String> title = Optional.empty();

    @Size(max = 255, message = "Synopsis cannot exceed 255 characters.")
    private Optional<String> synopsis = Optional.empty();

    @Positive(message = "Release year must be a positive number.")
    private Optional<Integer> releaseYear = Optional.empty();

    @Positive(message = "Duration must be a positive number.")
    private Optional<Integer> duration = Optional.empty();

    @Pattern(regexp = "AL|A10|A12|A14|A16|A18|OTHER", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid content rating value. Must be one of: AL, A10, A12, A14, A16, A18, OTHER.")
    private Optional<String> contentRating = Optional.empty();

    private Optional<Set<Long>> genreIds = Optional.empty();
}