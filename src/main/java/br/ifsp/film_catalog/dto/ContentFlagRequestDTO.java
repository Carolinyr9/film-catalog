package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentFlagRequestDTO {

    @NotBlank(message = "Flag reason cannot be blank.")
    @Size(min = 10, max = 255, message = "Flag reason must be between 10 and 255 characters.")
    private String flagReason;

    // reporterUserId will be taken from the authenticated principal
    // reviewId will be a path variable
}
