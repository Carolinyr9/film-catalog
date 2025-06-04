package br.ifsp.film_catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentFlagResponseDTO {
    private Long reporterUserId;
    private String reporterUsername; // Optional: to show who reported
    private Long reviewId;
    private String flagReason;
    private Instant createdAt;
    private Instant updatedAt;
}
