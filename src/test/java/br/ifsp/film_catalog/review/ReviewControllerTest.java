package br.ifsp.film_catalog.review;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.film_catalog.config.SecurityService;
import br.ifsp.film_catalog.controller.ReviewController;
import br.ifsp.film_catalog.dto.ContentFlagRequestDTO;
import br.ifsp.film_catalog.dto.ContentFlagResponseDTO;
import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.security.UserAuthenticated;
import br.ifsp.film_catalog.service.ContentFlagService;
import br.ifsp.film_catalog.service.ReviewService;
import br.ifsp.film_catalog.user.CustomUserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private ContentFlagService contentFlagService; 

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewResponseDTO exampleReview;

    @BeforeEach
    void setup() {
        exampleReview = ReviewResponseDTO.builder()
            .id(1L)
            .userId(1L)
            .movieId(10L)
            .generalScore(4)              // campo correto para nota
            .content("Muito bom filme!") // campo correto para comentário
            .likesCount(5)
            .build();
    }

    // método getAuthentication permanece igual se usado

    @Test
@WithMockUser(username = "joaosilva", roles = "USER")
    void createReview_shouldReturnCreatedReview_whenValid() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setContent("Ótimo filme!");
        request.setDirectionScore(4);
        request.setScreenplayScore(5);
        request.setCinematographyScore(4);
        request.setGeneralScore(5);

        // Ajuste aqui para o método correto que valida autorização
        when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

        when(reviewService.createReview(eq(1L), eq(10L), any(ReviewRequestDTO.class)))
                .thenReturn(exampleReview);

        mockMvc.perform(post("/api/users/1/movies/10/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(exampleReview.getId()))
                .andExpect(jsonPath("$.content").value(exampleReview.getContent()));

        verify(reviewService).createReview(eq(1L), eq(10L), any(ReviewRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER", username = "joaosilva")
    void updateReview_shouldReturnUpdatedReview_whenOwner() throws Exception {
        ReviewRequestDTO updateRequest = new ReviewRequestDTO();
        updateRequest.setGeneralScore(3);
        updateRequest.setDirectionScore(3);
        updateRequest.setScreenplayScore(3);
        updateRequest.setCinematographyScore(3);
        updateRequest.setContent("Atualizei a avaliação");

        ReviewResponseDTO updatedReview = ReviewResponseDTO.builder()
                .id(1L)
                .userId(1L)
                .movieId(10L)
                .generalScore(3)
                .content("Atualizei a avaliação")
                .likesCount(5)
                .build();

        when(reviewService.updateReview(eq(1L), eq(1L), any(ReviewRequestDTO.class)))
                .thenReturn(updatedReview);

        when(securityService.isReviewOwner(any(), eq(1L))).thenReturn(true);

        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(request -> {
                    request.setAttribute("userIdFromPrincipal", 1L);
                    return request;
                })
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("Atualizei a avaliação"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getReviewById_shouldReturnReview_whenExists() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(exampleReview);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exampleReview.getId()))
                .andExpect(jsonPath("$.content").value(exampleReview.getContent()));

        verify(reviewService).getReviewById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteReview_shouldReturnNoContent_whenOwner() throws Exception {
        // Simula sucesso no delete (void)
        doNothing().when(reviewService).deleteReview(eq(1L), eq(1L));
        when(securityService.isReviewOwner(any(), eq(1L))).thenReturn(true);

        mockMvc.perform(delete("/api/reviews/1")
                .with(request -> {
                    request.setAttribute("userIdFromPrincipal", 1L);
                    return request;
                }))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void likeReview_shouldReturnReviewWithIncrementedLikes() throws Exception {
        ReviewResponseDTO likedReview = ReviewResponseDTO.builder()
                .id(1L)
                .userId(1L)
                .movieId(10L)
                .generalScore(4)
                .content("Muito bom filme!")
                .likesCount(6) // incremento de likes
                .build();

        when(reviewService.likeReview(1L)).thenReturn(likedReview);

        mockMvc.perform(post("/api/reviews/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(6));

        verify(reviewService).likeReview(1L);
    }

    @Test
    void flagReview_shouldReturnCreatedFlag_whenValid() throws Exception {
        ContentFlagRequestDTO flagRequest = new ContentFlagRequestDTO();
        flagRequest.setFlagReason("Conteúdo ofensivo");

        ContentFlagResponseDTO flagResponse = ContentFlagResponseDTO.builder()
                .reviewId(1L)
                .reporterUserId(1L)
                .flagReason("Conteúdo ofensivo")
                .build();

        User user = new User();
        user.setId(1L);
        UserAuthenticated userAuthenticated = new UserAuthenticated(user);

        when(contentFlagService.flagReview(eq(1L), eq(1L), any(ContentFlagRequestDTO.class)))
                .thenReturn(flagResponse);

        mockMvc.perform(post("/api/reviews/1/flag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(flagRequest))
                .with(authentication(new UsernamePasswordAuthenticationToken(userAuthenticated, null, List.of())))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flagReason").value("Conteúdo ofensivo"));

        verify(contentFlagService).flagReview(eq(1L), eq(1L), any(ContentFlagRequestDTO.class));
    }

}
