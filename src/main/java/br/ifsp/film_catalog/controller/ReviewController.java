package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.ReviewDTO;
import br.ifsp.film_catalog.dto.WatchlistDTO;
import br.ifsp.film_catalog.exception.InvalidMovieStateException;
import br.ifsp.film_catalog.exception.InvalidReviewStateException;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.model.Review;
import br.ifsp.film_catalog.repository.ReviewRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.WatchlistRepository;
import br.ifsp.film_catalog.security.UserAuthenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Tag(name = "Reviews", description = "API para catalogo de filmes")
@Validated
@RestController
@RequestMapping("/api/movies/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ModelMapper reviewMapper;
    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;

    @Autowired
    public ReviewController(ReviewRepository reviewRepository, ModelMapper reviewMapper, UserRepository userRepository, WatchlistRepository watchlistRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
        this.watchlistRepository = watchlistRepository;
    }

    @Operation(summary = "Cria uma nova review para o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review criada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário ou Watchlist não encontrados", content = @Content),
            @ApiResponse(responseCode = "422", description = "Filme ainda não assistido", content = @Content)
    })
    @PostMapping("/register/{userId}")
    public ResponseEntity<?> createReviewByUser(@PathVariable Long userId,
                                                @AuthenticationPrincipal UserAuthenticated authentication,
                                                @RequestBody @Validated ReviewDTO dto) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getWatchlistId() != null) {
            var watchlist = watchlistRepository.findById(dto.getWatchlistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Watchlist não encontrada"));

            if (!watchlist.movieIsWatched(dto.getMovieId())) {
                throw new InvalidMovieStateException("Esse filme ainda não foi assistido");
            }
        }

        Review review = reviewMapper.map(dto, Review.class);
        review.setUser(user);
        reviewRepository.save(review);

        return ResponseEntity.ok("Review criada com sucesso");
    }

    @Operation(summary = "Listar todas as reviews de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews encontradas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<Iterable<Review>> getAllReviewsByUser(@PathVariable Long userId,
                                                                @AuthenticationPrincipal UserAuthenticated authentication,
                                                                Pageable pageable) {
        var reviews = reviewRepository.findAllByUser_Id(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Curtir review", description = "Incrementa a quantidade de curtidas de uma review específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review curtida com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "404", description = "Review não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PatchMapping("/like/{reviewId}")
    public ResponseEntity<Review> likeReview(@PathVariable Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review não encontrada"));
        review.setCurtidas(review.getCurtidas() + 1);
        reviewRepository.save(review);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Reportar review", description = "Incrementa a quantidade de denúncias de uma review específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review denunciada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "404", description = "Review não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PatchMapping("/report/{reviewId}")
    public ResponseEntity<Review> reportReview(@PathVariable Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review não encontrada"));

        review.setDenuncias(review.getDenuncias() + 1);

        if (review.getDenuncias() >= review.getMIN_DENUNCIAS() && !review.isOculta()) {
            review.setOculta(true);
        }

        reviewRepository.save(review);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Deletar review", description = "Deleta uma review específica")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deletada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "404", description = "Review não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Review> deleteReview(@PathVariable Long userId,
                                               @PathVariable Long reviewId) throws AccessDeniedException {
        var review = getUserReviewOrThrow(userId, reviewId);

        reviewRepository.delete(review);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar review", description = "Atualiza uma review específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "404", description = "Review não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(@PathVariable Long userId,
                                               @PathVariable Long reviewId,
                                               @RequestBody @Validated ReviewDTO dto) throws AccessDeniedException {
        var review = getUserReviewOrThrow(userId, reviewId);
        Review updated = reviewMapper.map(dto, Review.class);
        updated.setId(reviewId);
        updated.setUser(review.getUser());
        reviewRepository.save(updated);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Oculta uma review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review ocultada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Review não encontrada", content = @Content),
            @ApiResponse(responseCode = "422", description = "Review não pode ser ocultada", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/hide/{reviewId}")
    public ResponseEntity<Review> hideReview(@PathVariable Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review não encontrada"));

        if (review.getDenuncias() >= review.getMIN_DENUNCIAS()) {
            review.setOculta(true);
            reviewRepository.save(review);
            return ResponseEntity.ok(review);
        } else {
            throw new InvalidReviewStateException("Review não pode ser ocultada pois ainda não atingiu o número mínimo de denúncias.");
        }
    }

    private Review getUserReviewOrThrow(Long userId, Long reviewId) throws AccessDeniedException {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review não encontrada"));
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Review não pertence ao usuário");
        }
        return review;
    }

}
