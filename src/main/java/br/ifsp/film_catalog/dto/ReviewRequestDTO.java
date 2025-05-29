package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @NotBlank(message = "Review content cannot be blank.")
    @Size(max = 5000, message = "Review content cannot exceed 5000 characters.")
    private String content;

    @NotNull(message = "Direction score cannot be null.")
    @Min(value = 0, message = "Direction score must be at least 0.")
    @Max(value = 5, message = "Direction score must be at most 5.")
    private Integer directionScore;

    @NotNull(message = "Screenplay score cannot be null.")
    @Min(value = 0, message = "Screenplay score must be at least 0.")
    @Max(value = 5, message = "Screenplay score must be at most 5.")
    private Integer screenplayScore;

    @NotNull(message = "Cinematography score cannot be null.")
    @Min(value = 0, message = "Cinematography score must be at least 0.")
    @Max(value = 5, message = "Cinematography score must be at most 5.")
    private Integer cinematographyScore;

    @NotNull(message = "General score cannot be null.")
    @Min(value = 0, message = "General score must be at least 0.")
    @Max(value = 5, message = "General score must be at most 5.")
    private Integer generalScore;

    // userId and movieId will be path variables in the controller for creation
    // For update, reviewId will be a path variable.
}
