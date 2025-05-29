package br.ifsp.film_catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentFlagResponseDTO {
    private Long reporterUserId;
    private String reporterUsername; // Optional: to show who reported
    private Long reviewId;
    private String flagReason;
    private Instant createdAt;
    private Instant updatedAt;
    private LocalDateTime createdAtLocal; // For easier display in UI
    private LocalDateTime updatedAtLocal; // For easier display in UI
}
