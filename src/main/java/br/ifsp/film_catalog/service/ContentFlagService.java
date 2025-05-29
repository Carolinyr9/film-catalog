package br.ifsp.film_catalog.service;

import br.ifsp.film_catalog.dto.ContentFlagRequestDTO;
import br.ifsp.film_catalog.dto.ContentFlagResponseDTO;
import br.ifsp.film_catalog.dto.FlaggedReviewResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.InvalidReviewStateException;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.ContentFlag;
import br.ifsp.film_catalog.model.Review;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.ContentFlagRepository;
import br.ifsp.film_catalog.repository.ReviewRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentFlagService {

    private final ContentFlagRepository contentFlagRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService; // To use convertToResponseDTO and toggleHide
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    @Value("${app.reviews.flags.auto-hide-threshold:10}") // Default to 10 if not set in properties
    private int autoHideThreshold;

    public ContentFlagService(ContentFlagRepository contentFlagRepository,
                              ReviewRepository reviewRepository,
                              UserRepository userRepository,
                              ReviewService reviewService,
                              ModelMapper modelMapper,
                              PagedResponseMapper pagedResponseMapper) {
        this.contentFlagRepository = contentFlagRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.reviewService = reviewService; // Inject ReviewService
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public ContentFlagResponseDTO flagReview(Long reviewId, Long reporterUserId, ContentFlagRequestDTO requestDTO) {
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter User not found with id: " + reporterUserId));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (contentFlagRepository.existsById_ReviewIdAndId_UserId(reviewId, reporterUserId)) {
            throw new InvalidReviewStateException("User has already flagged this review.");
        }

        // Check if user is trying to flag their own review
        if (review.getUserWatched() != null && review.getUserWatched().getUser().getId().equals(reporterUserId)) {
            throw new InvalidReviewStateException("Users cannot flag their own reviews.");
        }

        ContentFlag contentFlag = new ContentFlag(reporter, review, requestDTO.getFlagReason());
        ContentFlag savedFlag = contentFlagRepository.save(contentFlag);

        // Check if the review should be auto-hidden
        long currentFlags = review.getFlags().size(); // Hibernate manages this collection, so size is accurate after save
        if (currentFlags >= autoHideThreshold && !review.isHidden()) {
            reviewService.toggleHideReview(reviewId, true); // Use ReviewService to hide
        }

        return modelMapper.map(savedFlag, ContentFlagResponseDTO.class);
    }
    
    @Transactional(readOnly = true)
    public PagedResponse<FlaggedReviewResponseDTO> getHeavilyFlaggedReviews(int minFlags, Pageable pageable) {
        List<Review> flaggedReviews = reviewRepository.findReviewsWithMinimumFlagsOrderByFlagsDesc(minFlags);
        
        if (flaggedReviews.isEmpty()) {
            return pagedResponseMapper.toPagedResponse(null, FlaggedReviewResponseDTO.class);
        }

        List<FlaggedReviewResponseDTO> flaggedReviewDTOs = flaggedReviews.stream()
                .map(review -> {
                    FlaggedReviewResponseDTO dto = modelMapper.map(review, FlaggedReviewResponseDTO.class);
                    dto.setFlagCount((long) review.getFlags().size());
                    return dto;
                })
                .collect(Collectors.toList());

        Page<FlaggedReviewResponseDTO> flaggedReviewsPage = new PageImpl<>(flaggedReviewDTOs, pageable, flaggedReviewDTOs.size());

        return pagedResponseMapper.toPagedResponse(flaggedReviewsPage, FlaggedReviewResponseDTO.class);
    }
}
