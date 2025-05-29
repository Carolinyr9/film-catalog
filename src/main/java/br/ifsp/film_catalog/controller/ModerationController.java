package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.FlaggedReviewResponseDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.service.ContentFlagService;
import br.ifsp.film_catalog.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Moderação", description = "API para funcionalidades de moderação (Admin)")
@Validated
@RestController
@RequestMapping("/api/moderation")
@PreAuthorize("hasRole('ADMIN')")
public class ModerationController {

    private final ReviewService reviewService;
    private final ContentFlagService contentFlagService;

    public ModerationController(ReviewService reviewService, ContentFlagService contentFlagService) {
        this.reviewService = reviewService;
        this.contentFlagService = contentFlagService;
    }

    @Operation(summary = "Listar avaliações altamente sinalizadas (Admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de avaliações sinalizadas recuperada"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/reviews/flagged")
    public ResponseEntity<PagedResponse<FlaggedReviewResponseDTO>> getHeavilyFlaggedReviews(
            @RequestParam(defaultValue = "10") int minFlags, // Pode ser configurável ou fixo
            @PageableDefault(size = 10) Pageable pageable) {
        PagedResponse<FlaggedReviewResponseDTO> reviews = contentFlagService.getHeavilyFlaggedReviews(minFlags, pageable);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Ocultar uma avaliação (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de ocultação da avaliação atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PatchMapping("/reviews/{reviewId}/hide")
    public ResponseEntity<ReviewResponseDTO> hideReview(@PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.toggleHideReview(reviewId, true);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Mostrar uma avaliação previamente oculta (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de ocultação da avaliação atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PatchMapping("/reviews/{reviewId}/unhide")
    public ResponseEntity<ReviewResponseDTO> unhideReview(@PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.toggleHideReview(reviewId, false);
        return ResponseEntity.ok(review);
    }
}
