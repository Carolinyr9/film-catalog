package br.ifsp.film_catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Assuming Review entity will have created/updated timestamps

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long id;
    private String content;
    private Integer directionScore;
    private Integer screenplayScore;
    private Integer cinematographyScore;
    private Integer generalScore;
    private int likesCount;
    private boolean hidden;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long userId;
    private String username;
    private Long movieId;
    private String movieTitle;
}
