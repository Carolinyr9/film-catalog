package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reviews", description = "API para gerenciamento de avaliações de filmes")
@Validated
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Criar uma nova avaliação para um filme assistido por um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não pode avaliar por outro)"),
            @ApiResponse(responseCode = "404", description = "Usuário ou Filme não encontrado"),
            @ApiResponse(responseCode = "409", description = "Filme não assistido pelo usuário ou já avaliado")
    })
    @PostMapping("/users/{userId}/movies/{movieId}/reviews")
    @PreAuthorize("hasRole('USER') and @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable Long userId,
            @PathVariable Long movieId,
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO) {
        ReviewResponseDTO createdReview = reviewService.createReview(userId, movieId, reviewRequestDTO);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @Operation(summary = "Obter uma avaliação específica pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Listar todas as avaliações (não ocultas) para um filme específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliações recuperadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado")
    })
    @GetMapping("/movies/{movieId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponseDTO>> getReviewsByMovie(
            @PathVariable Long movieId,
            @PageableDefault(size = 10, sort = "likesCount") Pageable pageable) {
        PagedResponse<ReviewResponseDTO> reviews = reviewService.getReviewsByMovie(movieId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Listar todas as avaliações (não ocultas) feitas por um usuário específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliações recuperadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/users/{userId}/reviews")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<PagedResponse<ReviewResponseDTO>> getReviewsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PagedResponse<ReviewResponseDTO> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Atualizar uma avaliação existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o proprietário da avaliação)"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PutMapping("/reviews/{reviewId}")
    // userId is passed from the authenticated principal by securityService.isReviewOwner
    @PreAuthorize("@securityService.isReviewOwner(authentication, #reviewId)")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            @RequestAttribute("userIdFromPrincipal") Long userId // Injetado pelo SecurityService (ver abaixo)
    ) {
        ReviewResponseDTO updatedReview = reviewService.updateReview(reviewId, userId, reviewRequestDTO);
        return ResponseEntity.ok(updatedReview);
    }

    @Operation(summary = "Deletar uma avaliação")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Avaliação deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é proprietário nem admin)"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("@securityService.isReviewOwner(authentication, #reviewId)")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
             @RequestAttribute(name = "userIdFromPrincipal", required = false) Long userIdPrincipal // Injetado pelo SecurityService
    ) {
        // O userIdPrincipal é usado pelo securityService.isReviewOwner.
        // O service deleteReview pode usar o userId do principal se precisar, mas a autorização já foi feita.
        reviewService.deleteReview(reviewId, userIdPrincipal);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Curtir uma avaliação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação curtida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PostMapping("/reviews/{reviewId}/like")
    @PreAuthorize("isAuthenticated()") // Qualquer usuário autenticado pode curtir
    public ResponseEntity<ReviewResponseDTO> likeReview(@PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.likeReview(reviewId);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Ocultar uma avaliação (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de ocultação da avaliação atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PatchMapping("/reviews/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDTO> hideReview(@PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.toggleHideReview(reviewId, true);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Mostrar uma avaliação previamente oculta (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de ocultação da avaliação atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PatchMapping("/reviews/{reviewId}/unhide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDTO> unhideReview(@PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.toggleHideReview(reviewId, false);
        return ResponseEntity.ok(review);
    }
}