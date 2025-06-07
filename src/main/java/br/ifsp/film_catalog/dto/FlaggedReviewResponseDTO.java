package br.ifsp.film_catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggedReviewResponseDTO {
    private ReviewResponseDTO review; // The actual review details
    private Long flagCount;      // Total number of flags for this review
}
