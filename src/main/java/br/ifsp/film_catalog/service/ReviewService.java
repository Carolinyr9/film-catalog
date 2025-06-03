package br.ifsp.film_catalog.service;

import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.InvalidReviewStateException;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Review;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.UserWatched;
import br.ifsp.film_catalog.model.key.UserMovieId;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.ReviewRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.UserWatchedRepository;
import jakarta.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserWatchedRepository userWatchedRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         MovieRepository movieRepository,
                         UserWatchedRepository userWatchedRepository,
                         ModelMapper modelMapper,
                         PagedResponseMapper pagedResponseMapper) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.userWatchedRepository = userWatchedRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public ReviewResponseDTO createReview(Long userId, Long movieId, ReviewRequestDTO reviewRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        UserMovieId userMovieId = new UserMovieId(user.getId(), movie.getId());
        UserWatched userWatched = userWatchedRepository.findById(userMovieId)
                .orElseThrow(() -> new InvalidReviewStateException("User has not watched this movie. Cannot create review."));

        // Check if a review already exists for this UserWatched entry
        if (userWatched.getReview() != null) {
            throw new InvalidReviewStateException("A review already exists for this watched movie by this user.");
        }

        Review review = modelMapper.map(reviewRequestDTO, Review.class);
        review.setUserWatched(userWatched); // Link review to UserWatched

        // The UserWatched entity needs to be updated with the review
        // and the Review entity needs to be saved.
        // The cascade from UserWatched to Review should handle saving the Review.
        userWatched.setReview(review); // Establish bidirectional link
        userWatchedRepository.save(userWatched); // Saving UserWatched will cascade persist to Review

        return modelMapper.map(userWatched.getReview(), ReviewResponseDTO.class); // Return DTO of the newly created review
    }

    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        return modelMapper.map(review, ReviewResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDTO> getReviewsByMovie(Long movieId, Pageable pageable) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        Page<Review> reviewPage = reviewRepository.findByUserWatched_Movie_IdAndHiddenFalse(movieId, pageable);
        return pagedResponseMapper.toPagedResponse(reviewPage, ReviewResponseDTO.class);
    }
    
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDTO> getReviewsByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        Page<Review> reviewPage = reviewRepository.findByUserWatched_User_IdAndHiddenFalse(userId, pageable);
        return pagedResponseMapper.toPagedResponse(reviewPage, ReviewResponseDTO.class);
    }

    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, Long userId, ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUserWatched().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to update this review.");
        }

        modelMapper.map(reviewRequestDTO, review);

        Review updatedReview = reviewRepository.save(review);
        return modelMapper.map(updatedReview, ReviewResponseDTO.class);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userIdPrincipal) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        UserWatched userWatched = review.getUserWatched();
        if (userWatched != null) {
            userWatched.setReview(null);
            userWatchedRepository.save(userWatched);
        }
        reviewRepository.delete(review);
    }

    @Transactional
    public ReviewResponseDTO likeReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setLikesCount(review.getLikesCount() + 1);
        Review likedReview = reviewRepository.save(review);
        return modelMapper.map(likedReview, ReviewResponseDTO.class);
    }

    @Transactional
    public ReviewResponseDTO toggleHideReview(Long reviewId, boolean hide) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setHidden(hide);
        Review updatedReview = reviewRepository.save(review);
        return modelMapper.map(updatedReview, ReviewResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public void exportAsPdf(HttpServletResponse response) throws Exception {
        List<Review> reviews = reviewRepository.findAll();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reviews.pdf");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);

        document.open();
        document.add(new Paragraph("Lista de Avaliações"));

        for (Review review : reviews) {
            document.add(new Paragraph("Review ID: " + review.getId()));
            document.add(new Paragraph("Usuário: " + review.getUserWatched().getUser().getUsername()));
            document.add(new Paragraph("Filme: " + review.getUserWatched().getMovie().getTitle()));
            document.add(new Paragraph("Nota Geral: " + review.getGeneralScore()));
            document.add(new Paragraph("Nota de Direção: " + review.getDirectionScore()));
            document.add(new Paragraph("Nota de Roteiro: " + review.getScreenplayScore()));
            document.add(new Paragraph("Nota de Cinematografia: " + review.getCinematographyScore()));
            document.add(new Paragraph("Conteúdo: " + review.getContent()));
            document.add(new Paragraph("------"));
        }

        document.close();
        OutputStream os = response.getOutputStream();
        baos.writeTo(os);
        os.flush();
    }

    public String getUserStatistics(Pageable pageable, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long totalReviews = reviewRepository.countByUserWatched_User_Id(userId);
        long totalLikes = reviewRepository.sumLikesCountByUserWatched_User_Id(userId);
        double averageGeneralScore = reviewRepository.calculateAverageGeneralScoreByUserWatched_User_Id(userId);

        return String.format("O usuário %s fez %d reviews, recebeu %d likes e tem uma média geral nas avaliações de %.2f.", user.getUsername(), totalReviews, totalLikes, averageGeneralScore);
    }

    public List<Double> getAverageWeighted(Pageable pageable, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        double directionAvg = reviewRepository.calculateAverageDirectionScoreByUserWatched_User_Id(userId);
        double screenplayAvg = reviewRepository.calculateAverageScreenplayScoreByUserWatched_User_Id(userId);
        double cinematographyAvg = reviewRepository.calculateAverageCinematographyScoreByUserWatched_User_Id(userId);
        double generalAvg = reviewRepository.calculateAverageGeneralScoreByUserWatched_User_Id(userId);

        List<Double> averages = new ArrayList<>();
        averages.add(directionAvg);
        averages.add(screenplayAvg);
        averages.add(cinematographyAvg);
        averages.add(generalAvg);

        return averages;
    }


}
