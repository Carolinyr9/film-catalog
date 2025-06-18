package br.ifsp.film_catalog.review;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
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
import br.ifsp.film_catalog.service.ReviewService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserWatchedRepository userWatchedRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_success() {
        Long userId = 1L;
        Long movieId = 10L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Test Movie");

        UserMovieId userMovieId = new UserMovieId(userId, movieId);

        UserWatched userWatched = new UserWatched();
        userWatched.setId(userMovieId);
        userWatched.setUser(user);
        userWatched.setMovie(movie);

        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setContent("Great movie!");
        reviewRequestDTO.setDirectionScore(8);
        reviewRequestDTO.setScreenplayScore(7);
        reviewRequestDTO.setCinematographyScore(9);
        reviewRequestDTO.setGeneralScore(8);

        Review mappedReview = new Review();
        mappedReview.setContent(reviewRequestDTO.getContent());
        mappedReview.setDirectionScore(reviewRequestDTO.getDirectionScore());
        mappedReview.setScreenplayScore(reviewRequestDTO.getScreenplayScore());
        mappedReview.setCinematographyScore(reviewRequestDTO.getCinematographyScore());
        mappedReview.setGeneralScore(reviewRequestDTO.getGeneralScore());

        ReviewResponseDTO responseDTO = new ReviewResponseDTO();
        responseDTO.setContent(mappedReview.getContent());
        responseDTO.setDirectionScore(mappedReview.getDirectionScore());
        responseDTO.setScreenplayScore(mappedReview.getScreenplayScore());
        responseDTO.setCinematographyScore(mappedReview.getCinematographyScore());
        responseDTO.setGeneralScore(mappedReview.getGeneralScore());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(userWatchedRepository.findById(userMovieId)).thenReturn(Optional.of(userWatched));
        when(modelMapper.map(reviewRequestDTO, Review.class)).thenReturn(mappedReview);
        when(modelMapper.map(any(Review.class), eq(ReviewResponseDTO.class))).thenReturn(responseDTO);

        ReviewResponseDTO result = reviewService.createReview(userId, movieId, reviewRequestDTO);

        assertNotNull(result);
        assertEquals(reviewRequestDTO.getContent(), result.getContent());
        verify(userWatchedRepository).save(userWatched);
        verify(modelMapper).map(reviewRequestDTO, Review.class);
        verify(modelMapper).map(any(Review.class), eq(ReviewResponseDTO.class));
    }

    @Test
    void createReview_userNotFound_throwsResourceNotFoundException() {
        Long userId = 1L;
        Long movieId = 10L;
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> reviewService.createReview(userId, movieId, reviewRequestDTO));
        assertTrue(ex.getMessage().contains("User not found with id"));
    }

    @Test
    void createReview_movieNotFound_throwsResourceNotFoundException() {
        Long userId = 1L;
        Long movieId = 10L;
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> reviewService.createReview(userId, movieId, reviewRequestDTO));
        assertTrue(ex.getMessage().contains("Movie not found with id"));
    }

    @Test
    void createReview_userWatchedNotFound_throwsInvalidReviewStateException() {
        Long userId = 1L;
        Long movieId = 10L;
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();

        User user = new User();
        user.setId(userId);
        Movie movie = new Movie();
        movie.setId(movieId);

        UserMovieId userMovieId = new UserMovieId(userId, movieId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(userWatchedRepository.findById(userMovieId)).thenReturn(Optional.empty());

        InvalidReviewStateException ex = assertThrows(InvalidReviewStateException.class,
            () -> reviewService.createReview(userId, movieId, reviewRequestDTO));
        assertTrue(ex.getMessage().contains("User has not watched this movie"));
    }

    @Test
    void createReview_reviewAlreadyExists_throwsInvalidReviewStateException() {
        Long userId = 1L;
        Long movieId = 10L;
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();

        User user = new User();
        user.setId(userId);
        Movie movie = new Movie();
        movie.setId(movieId);

        UserMovieId userMovieId = new UserMovieId(userId, movieId);

        UserWatched userWatched = new UserWatched();
        userWatched.setId(userMovieId);
        userWatched.setUser(user);
        userWatched.setMovie(movie);
        userWatched.setReview(new Review());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(userWatchedRepository.findById(userMovieId)).thenReturn(Optional.of(userWatched));

        InvalidReviewStateException ex = assertThrows(InvalidReviewStateException.class,
            () -> reviewService.createReview(userId, movieId, reviewRequestDTO));
        assertTrue(ex.getMessage().contains("A review already exists"));
    }
    
    @Test
    void getReviewById_success() {
        Long reviewId = 5L;
        Review review = new Review();
        review.setId(reviewId);
        ReviewResponseDTO responseDTO = new ReviewResponseDTO();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(modelMapper.map(review, ReviewResponseDTO.class)).thenReturn(responseDTO);

        ReviewResponseDTO result = reviewService.getReviewById(reviewId);

        assertNotNull(result);
        verify(reviewRepository).findById(reviewId);
        verify(modelMapper).map(review, ReviewResponseDTO.class);
    }

    @Test
    void getReviewById_notFound_throwsResourceNotFoundException() {
        Long reviewId = 5L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> reviewService.getReviewById(reviewId));
        assertTrue(ex.getMessage().contains("Review not found with id"));
    }

    @Test
void updateReview_success() {
    Long reviewId = 1L;
    Long userId = 10L;
    ReviewRequestDTO dto = new ReviewRequestDTO();
    Review review = new Review();
    review.setId(reviewId);

    User user = new User();
    user.setId(userId);
    UserWatched userWatched = new UserWatched();
    userWatched.setUser(user);
    review.setUserWatched(userWatched);

    Review updatedReview = new Review();
    ReviewResponseDTO responseDTO = new ReviewResponseDTO();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    doAnswer(invocation -> {
        // Simula o map do ModelMapper (pode ser vazio)
        return null;
    }).when(modelMapper).map(dto, review);
    when(reviewRepository.save(review)).thenReturn(updatedReview);
    when(modelMapper.map(updatedReview, ReviewResponseDTO.class)).thenReturn(responseDTO);

    ReviewResponseDTO result = reviewService.updateReview(reviewId, userId, dto);

    assertNotNull(result);
    verify(reviewRepository).findById(reviewId);
    verify(modelMapper).map(dto, review);
    verify(reviewRepository).save(review);
}

    @Test
    void updateReview_unauthorizedUser_throwsAccessDeniedException() {
        Long reviewId = 1L;
        Long userId = 10L;
        ReviewRequestDTO dto = new ReviewRequestDTO();
        Review review = new Review();

        User user = new User();
        user.setId(999L); // Id diferente do userId do parâmetro
        UserWatched userWatched = new UserWatched();
        userWatched.setUser(user);
        review.setUserWatched(userWatched);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
            () -> reviewService.updateReview(reviewId, userId, dto));
        assertTrue(ex.getMessage().contains("User is not authorized"));
    }

    @Test
    void deleteReview_success() {
        Long reviewId = 1L;

        Review review = new Review();
        review.setId(reviewId);

        UserWatched userWatched = new UserWatched();
        review.setUserWatched(userWatched);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId, 123L);

        verify(userWatchedRepository).save(userWatched);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_notFound_throwsResourceNotFoundException() {
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> reviewService.deleteReview(reviewId, 123L));
        assertTrue(ex.getMessage().contains("Review not found"));
    }

    @Test
    void likeReview_success() {
        Long reviewId = 1L;

        Review review = new Review();
        review.setId(reviewId);
        review.setLikesCount(5);

        Review savedReview = new Review();
        savedReview.setLikesCount(6);

        ReviewResponseDTO responseDTO = new ReviewResponseDTO();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(savedReview);
        when(modelMapper.map(savedReview, ReviewResponseDTO.class)).thenReturn(responseDTO);

        ReviewResponseDTO result = reviewService.likeReview(reviewId);

        assertNotNull(result);
        assertEquals(6, savedReview.getLikesCount());
    }

    @Test
    void toggleHideReview_success() {
        Long reviewId = 1L;
        boolean hide = true;

        Review review = new Review();
        review.setId(reviewId);
        review.setHidden(false);

        Review updatedReview = new Review();
        updatedReview.setHidden(hide);

        ReviewResponseDTO responseDTO = new ReviewResponseDTO();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(updatedReview);
        when(modelMapper.map(updatedReview, ReviewResponseDTO.class)).thenReturn(responseDTO);

        ReviewResponseDTO result = reviewService.toggleHideReview(reviewId, hide);

        assertNotNull(result);
        assertTrue(updatedReview.isHidden());
    }

}
