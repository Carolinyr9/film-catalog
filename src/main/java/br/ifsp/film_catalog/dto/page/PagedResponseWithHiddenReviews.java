package br.ifsp.film_catalog.dto.page;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;

@Getter
@AllArgsConstructor
public class PagedResponseWithHiddenReviews {
    private List<ReviewResponseDTO> visibleReviews;
    private List<Long> hiddenReviewIds;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
