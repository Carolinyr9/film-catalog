package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.config.CustomUserDetails;
import br.ifsp.film_catalog.dto.ContentFlagRequestDTO;
import br.ifsp.film_catalog.dto.ContentFlagResponseDTO;
import br.ifsp.film_catalog.dto.ReviewAveragesDTO;
import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.dto.page.PagedResponseWithHiddenReviews;
import br.ifsp.film_catalog.exception.ErrorResponse;
import br.ifsp.film_catalog.security.UserAuthenticated;
import br.ifsp.film_catalog.service.ContentFlagService;
import br.ifsp.film_catalog.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Reviews", description = "API para gerenciamento de avaliações de filmes")
@Validated
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final ContentFlagService contentFlagService;

    public ReviewController(ReviewService reviewService, ContentFlagService contentFlagService) {
        this.reviewService = reviewService;
        this.contentFlagService = contentFlagService;
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
    public ResponseEntity<PagedResponseWithHiddenReviews> getReviewsByMovie(
            @PathVariable Long movieId,
            @PageableDefault(size = 10, sort = "likesCount") Pageable pageable) {
        PagedResponseWithHiddenReviews reviews = reviewService.getReviewsByMovie(movieId, pageable);
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
    public ResponseEntity<PagedResponseWithHiddenReviews> getReviewsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PagedResponseWithHiddenReviews reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Atualizar uma avaliação existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o proprietário da avaliação)"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @PreAuthorize("@securityService.isReviewOwner(authentication, #reviewId)")
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            Authentication authentication
    ) {
        Long userId = extractUserIdFromAuthentication(authentication);
        ReviewResponseDTO updatedReview = reviewService.updateReview(reviewId, userId, reviewRequestDTO);
        return ResponseEntity.ok(updatedReview);
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        UserAuthenticated userAuthenticated = (UserAuthenticated) authentication.getPrincipal();
        return userAuthenticated.getUser().getId();  
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

    @Operation(summary = "Sinalizar (flag) uma avaliação")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Avaliação sinalizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: razão em branco)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Avaliação ou usuário reportando não encontrado"),
        @ApiResponse(responseCode = "409", description = "Usuário já sinalizou esta avaliação ou tentando sinalizar a própria avaliação")
    })
    @PostMapping("/reviews/{reviewId}/flag")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContentFlagResponseDTO> flagReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ContentFlagRequestDTO contentFlagRequestDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated reportedBy) {
        System.out.println("Usuário autenticado ID: " + reportedBy.getUser().getId());
        System.out.println("Usuário autenticado Username: " + reportedBy.getUser().getUsername());

        ContentFlagResponseDTO flagged = contentFlagService.flagReview(reviewId, reportedBy.getUser().getId(), contentFlagRequestDTO);

        return new ResponseEntity<>(flagged, HttpStatus.CREATED);
    }

    @Operation(summary = "Exportar avaliações em PDF", description = "Exporta todas as avaliações no modelo pdf.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de avaiações exportada com sucesso",
                         content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public void exportAsPdf(HttpServletResponse response) throws Exception {
        reviewService.exportAsPdf(response);
    }

    @GetMapping("/reviews/{userId}/userStatistics")
    @Operation(summary = "Listar estatísticas de avaliações feitas por um usuário específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas recuperadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getUserStatistics(
            @PageableDefault(size = 10, sort = "reviewsCount") Pageable pageable,
            @PathVariable Long userId) {
        String userStatistics = reviewService.getUserStatistics(pageable, userId);
        return ResponseEntity.ok(userStatistics);
    }

    @Operation(summary = "Listar média ponderada das avaliações por critérios de um usuário específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas recuperadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/reviews/{userId}/average-weighted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewAveragesDTO> getAverageWeighted(
            @PageableDefault(size = 10, sort = "reviewsCount") Pageable pageable, 
            @PathVariable Long userId) {
        ReviewAveragesDTO reviewsStatistics = reviewService.getAverageWeighted(pageable, userId);
        return ResponseEntity.ok(reviewsStatistics);
    }

    
}