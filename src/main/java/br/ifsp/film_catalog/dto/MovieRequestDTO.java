package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Year;
import java.util.Set;

import br.ifsp.film_catalog.model.Genre;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDTO {
    @NotBlank(message = "Movie title cannot be blank.")
    @Size(max = 255, message = "Movie title cannot exceed 255 characters.")
    private String title;

    @Size(max = 255, message = "Synopsis cannot exceed 255 characters.")
    private String synopsis;

    @NotNull(message = "Release year cannot be null.")
    @Positive(message = "Release year must be a positive number.")
    @Max(value = 2025, message = "Release year cannot be in the future.") // Replace 2023 with the current year manually
    private Integer releaseYear;

    @NotNull(message = "Duration cannot be null.")
    @Positive(message = "Duration must be a positive number.")
    private Integer duration;

    @Pattern(regexp = "AL|A10|A12|A14|A16|A18|OTHER", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid content rating value. Must be one of: AL, A10, A12, A14, A16, A18, OTHER.")
    private String contentRating;

    @NotEmpty(message = "Must have at least one genre")
    private Set<Genre> genres;
}