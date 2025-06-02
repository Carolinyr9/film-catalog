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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
