package br.ifsp.film_catalog.review;

import br.ifsp.film_catalog.config.CustomUserDetails; // Importar CustomUserDetails (se ainda usar em outros lugares)
import br.ifsp.film_catalog.config.SecurityService;
import br.ifsp.film_catalog.dto.ContentFlagRequestDTO;
import br.ifsp.film_catalog.dto.ContentFlagResponseDTO;
import br.ifsp.film_catalog.dto.ReviewAveragesDTO;
import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponseWithHiddenReviews;
import br.ifsp.film_catalog.exception.InvalidReviewStateException;
// Importar as exceções customizadas (certifique-se de que estão definidas no seu projeto)
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.model.User; // **IMPORTAR SUA CLASSE USER**
import br.ifsp.film_catalog.model.Role; // **IMPORTAR SUA CLASSE ROLE** (se existir)
import br.ifsp.film_catalog.model.enums.RoleName; // **IMPORTAR SEU ENUM RoleName** (se existir)
import br.ifsp.film_catalog.security.UserAuthenticated; // **IMPORTAR SUA CLASSE UserAuthenticated**

import br.ifsp.film_catalog.service.ContentFlagService;
import br.ifsp.film_catalog.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
        private UserAuthenticated authenticatedUserPrincipal;
        private Long userId = 1L;
        private Long movieId = 10L;
        private Long reviewId = 1L;
        private Long nonExistentId = 999L;

        @BeforeEach
        void setup() {
                exampleReview = ReviewResponseDTO.builder()
                                .id(reviewId)
                                .userId(userId)
                                .movieId(movieId)
                                .generalScore(4)
                                .content("Muito bom filme!")
                                .likesCount(5)
                                .build();

                User principalUser = new User();
                principalUser.setId(userId);
                principalUser.setUsername("joaosilva");
                principalUser.setPassword("Password123!");
                principalUser.setEmail("joao.silva@example.com");

                Role userRole = new Role();
                userRole.setId(100L);
                userRole.setRoleName(RoleName.ROLE_USER);
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                principalUser.addRole(userRole);

                authenticatedUserPrincipal = new UserAuthenticated(principalUser);

                reset(reviewService, securityService, contentFlagService);
                when(securityService.isOwner(any(Authentication.class), eq("joaosilva"))).thenReturn(true);
                when(securityService.isReviewOwner(any(Authentication.class), eq(reviewId))).thenReturn(true);

                // Comportamento padrão para isOwner/isReviewOwner para outros IDs ou quando não
                // há stub específico: false
                when(securityService.isOwner(any(Authentication.class), anyString())).thenReturn(false);
                when(securityService.isReviewOwner(any(Authentication.class), anyLong())).thenReturn(false);
        }

        // --- Métodos de Teste Já Existentes (Mantidos para referência) ---

        @Test
        @WithMockUser(username = "joaosilva", roles = "USER")
        void shouldReturn201AndReviewResponse_whenCreatingValidReview() throws Exception {
                ReviewRequestDTO request = new ReviewRequestDTO();
                request.setContent("Ótimo filme!");
                request.setGeneralScore(5);
                request.setDirectionScore(4);
                request.setScreenplayScore(5);
                request.setCinematographyScore(4);

                when(reviewService.createReview(eq(userId), eq(movieId), any(ReviewRequestDTO.class)))
                                .thenReturn(exampleReview);

                mockMvc.perform(post("/api/users/{userId}/movies/{movieId}/reviews", userId, movieId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(authentication(new UsernamePasswordAuthenticationToken(
                                                authenticatedUserPrincipal,
                                                null,
                                                authenticatedUserPrincipal.getAuthorities()))))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value(exampleReview.getId()))
                                .andExpect(jsonPath("$.content").value(exampleReview.getContent()))
                                .andExpect(jsonPath("$.generalScore").value(exampleReview.getGeneralScore()))
                                .andExpect(jsonPath("$.directionScore").value(exampleReview.getDirectionScore()))
                                .andExpect(jsonPath("$.screenplayScore").value(exampleReview.getScreenplayScore()))
                                .andExpect(jsonPath("$.cinematographyScore")
                                                .value(exampleReview.getCinematographyScore()))
                                .andExpect(jsonPath("$.movieId").value(exampleReview.getMovieId()));

                verify(reviewService).createReview(eq(userId), eq(movieId), any(ReviewRequestDTO.class));
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
                                .id(reviewId)
                                .userId(userId)
                                .movieId(movieId)
                                .generalScore(3)
                                .content("Atualizei a avaliação")
                                .likesCount(5)
                                .build();

                when(reviewService.updateReview(eq(reviewId), eq(userId), any(ReviewRequestDTO.class)))
                                .thenReturn(updatedReview);

                mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                                .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUserPrincipal,
                                                null, authenticatedUserPrincipal.getAuthorities()))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").value("Atualizei a avaliação"));

                verify(reviewService).updateReview(eq(reviewId), eq(userId), any(ReviewRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        void getReviewById_shouldReturnReview_whenExists() throws Exception {
                when(reviewService.getReviewById(reviewId)).thenReturn(exampleReview);

                mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(exampleReview.getId()))
                                .andExpect(jsonPath("$.content").value(exampleReview.getContent()));

                verify(reviewService).getReviewById(reviewId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void deleteReview_shouldReturnNoContent_whenOwner() throws Exception {
                doNothing().when(reviewService).deleteReview(eq(reviewId), eq(userId));

                when(securityService.isReviewOwner(any(Authentication.class), eq(reviewId))).thenReturn(true);

                mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                                .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUserPrincipal,
                                                null,
                                                authenticatedUserPrincipal.getAuthorities())))
                                .requestAttr("userIdFromPrincipal", userId))
                                .andExpect(status().isNoContent());

                verify(reviewService).deleteReview(reviewId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void likeReview_shouldReturnReviewWithIncrementedLikes() throws Exception {
                ReviewResponseDTO likedReview = ReviewResponseDTO.builder()
                                .id(reviewId)
                                .userId(userId)
                                .movieId(movieId)
                                .generalScore(4)
                                .content("Muito bom filme!")
                                .likesCount(6)
                                .build();

                when(reviewService.likeReview(reviewId)).thenReturn(likedReview);

                mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.likesCount").value(6));

                verify(reviewService).likeReview(reviewId);
        }

        @Test
        @WithMockUser(username = "joaosilva", roles = "USER")
        void flagReview_shouldReturnCreatedFlag_whenValid() throws Exception {
                ContentFlagRequestDTO flagRequest = new ContentFlagRequestDTO();
                flagRequest.setFlagReason("Conteúdo ofensivo");

                ContentFlagResponseDTO flagResponse = ContentFlagResponseDTO.builder()
                                .reviewId(reviewId)
                                .reporterUserId(userId)
                                .flagReason("Conteúdo ofensivo")
                                .build();

                when(contentFlagService.flagReview(eq(reviewId), eq(userId), any(ContentFlagRequestDTO.class)))
                                .thenReturn(flagResponse);

                mockMvc.perform(post("/api/reviews/{reviewId}/flag", reviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(flagRequest))
                                .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUserPrincipal,
                                                null, authenticatedUserPrincipal.getAuthorities()))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.flagReason").value("Conteúdo ofensivo"));

                verify(contentFlagService).flagReview(eq(reviewId), eq(userId), any(ContentFlagRequestDTO.class));
        }

        // --- Métodos de Teste a Serem Criados ---

        // --- createReview ---
        @Test
        // Sem @WithMockUser para simular usuário não autenticado
        void createReview_shouldReturn401_whenUnauthorized() throws Exception {
                ReviewRequestDTO request = new ReviewRequestDTO();
                request.setGeneralScore(5);
                request.setContent("Filme muito bom!");

                mockMvc.perform(post("/api/users/{userId}/movies/{movieId}/reviews", userId, movieId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "joaosilva", roles = "USER")
        void createReview_shouldReturn400_whenInvalidInput() throws Exception {
                ReviewRequestDTO invalidRequest = new ReviewRequestDTO();
                invalidRequest.setGeneralScore(10); // Pontuação inválida (ex: > 5)
                invalidRequest.setContent(""); // Conteúdo vazio/nulo

                when(reviewService.createReview(eq(userId), eq(movieId), any(ReviewRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("Dados de entrada inválidos"));

                mockMvc.perform(post("/api/users/{userId}/movies/{movieId}/reviews", userId, movieId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUserPrincipal,
                                                null, authenticatedUserPrincipal.getAuthorities()))))
                                .andExpect(status().isBadRequest());
        }

        // --- getReviewById ---
        @Test
        @WithMockUser(roles = "USER")
        void getReviewById_shouldReturn404_whenNotFound() throws Exception {
                when(reviewService.getReviewById(nonExistentId))
                                .thenThrow(new ResourceNotFoundException("Avaliação não encontrada"));
                mockMvc.perform(get("/api/reviews/{reviewId}", nonExistentId))
                                .andExpect(status().isNotFound());
        }

        @Test
        // Sem @WithMockUser (não autenticado)
        void getReviewById_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                                .andExpect(status().isUnauthorized());
        }

        // --- getReviewsByMovie ---
        @Test
        @WithMockUser(roles = "USER")
        void getReviewsByMovie_shouldReturn404_whenMovieNotFound() throws Exception {
                when(reviewService.getReviewsByMovie(eq(nonExistentId), any(Pageable.class)))
                                .thenThrow(new ResourceNotFoundException("Filme não encontrado"));
                mockMvc.perform(get("/api/movies/{movieId}/reviews", nonExistentId))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        void getReviewsByMovie_shouldReturn200AndEmptyList_whenNoReviews() throws Exception {
                PagedResponseWithHiddenReviews emptyResponse = new PagedResponseWithHiddenReviews(
                                Collections.emptyList(), Collections.emptyList(), 0, 10, 0L, 0, true);
                when(reviewService.getReviewsByMovie(eq(movieId), any(Pageable.class))).thenReturn(emptyResponse);
                mockMvc.perform(get("/api/movies/{movieId}/reviews", movieId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.visibleReviews", hasSize(0)));
        }

        @Test
        // Sem @WithMockUser
        void getReviewsByMovie_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies/{movieId}/reviews", movieId))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        void getReviewsByMovie_shouldHandlePaginationAndSorting() throws Exception {
                ReviewResponseDTO review1 = ReviewResponseDTO.builder().id(2L).userId(userId).movieId(movieId)
                                .generalScore(3).content("Review 1").build();
                ReviewResponseDTO review2 = ReviewResponseDTO.builder().id(3L).userId(userId).movieId(movieId)
                                .generalScore(5).content("Review 2").build();
                PagedResponseWithHiddenReviews pagedResponse = new PagedResponseWithHiddenReviews(List.of(review2),
                                Collections.emptyList(), 0, 1, 2L, 2, false);

                when(reviewService.getReviewsByMovie(eq(movieId), any(Pageable.class))).thenReturn(pagedResponse);

                mockMvc.perform(get("/api/movies/{movieId}/reviews", movieId)
                                .param("page", "0")
                                .param("size", "1")
                                .param("sort", "likesCount,desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.visibleReviews", hasSize(1)))
                                .andExpect(jsonPath("$.visibleReviews[0].id").value(review2.getId()));
        }

        // --- getReviewsByUser ---
        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        void getReviewsByUser_shouldReturn404_whenUserNotFound() throws Exception {
                when(reviewService.getReviewsByUser(eq(nonExistentId), any(Pageable.class)))
                                .thenThrow(new ResourceNotFoundException("Usuário não encontrado para reviews"));
                mockMvc.perform(get("/api/users/{userId}/reviews", nonExistentId))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "joaosilva", roles = "USER")
        void getReviewsByUser_shouldReturn200AndEmptyList_whenNoReviews() throws Exception {
                PagedResponseWithHiddenReviews emptyResponse = new PagedResponseWithHiddenReviews(
                                Collections.emptyList(), Collections.emptyList(), 0, 10, 0L, 0, true);
                when(reviewService.getReviewsByUser(eq(userId), any(Pageable.class))).thenReturn(emptyResponse);
                mockMvc.perform(get("/api/users/{userId}/reviews", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.visibleReviews", hasSize(0)));
        }

        @Test
        // Sem @WithMockUser
        void getReviewsByUser_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/users/{userId}/reviews", userId))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "joaosilva", roles = "USER")
        void getReviewsByUser_shouldHandlePaginationAndSorting() throws Exception {
                ReviewResponseDTO review1 = ReviewResponseDTO.builder().id(2L).userId(userId).movieId(movieId)
                                .generalScore(3).content("Review 1").build();
                PagedResponseWithHiddenReviews pagedResponse = new PagedResponseWithHiddenReviews(List.of(review1),
                                Collections.emptyList(), 0, 1, 2L, 2, false);

                when(reviewService.getReviewsByUser(eq(userId), any(Pageable.class))).thenReturn(pagedResponse);

                mockMvc.perform(get("/api/users/{userId}/reviews", userId)
                                .param("page", "0")
                                .param("size", "1")
                                .param("sort", "createdAt,desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.visibleReviews", hasSize(1)))
                                .andExpect(jsonPath("$.visibleReviews[0].id").value(review1.getId()));
        }

        // --- updateReview ---
        @Test
        @WithMockUser(roles = "USER")
        void updateReview_shouldReturn400_whenInvalidInput() throws Exception {
                ReviewRequestDTO invalidRequest = new ReviewRequestDTO();
                invalidRequest.setGeneralScore(10);

                when(reviewService.updateReview(eq(reviewId), eq(userId), any(ReviewRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("Dados de entrada inválidos"));

                mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUserPrincipal,
                                                null, authenticatedUserPrincipal.getAuthorities()))))
                                .andExpect(status().isBadRequest());
        }

        @Test
        // Sem @WithMockUser
        void updateReview_shouldReturn401_whenUnauthorized() throws Exception {
                ReviewRequestDTO request = new ReviewRequestDTO();
                request.setGeneralScore(3);
                request.setContent("Não autorizado.");

                mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        // Sem @WithMockUser
        void deleteReview_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId))
                                .andExpect(status().isUnauthorized());
        }

        // --- likeReview (cenários adicionais) ---
        @Test
        @WithMockUser(roles = "USER")
        void likeReview_shouldReturn404_whenReviewNotFound() throws Exception {
                when(reviewService.likeReview(nonExistentId))
                                .thenThrow(new ResourceNotFoundException("Avaliação não encontrada"));
                mockMvc.perform(post("/api/reviews/{reviewId}/like", nonExistentId))
                                .andExpect(status().isNotFound());
        }

        @Test
        // Sem @WithMockUser
        void likeReview_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                                .andExpect(status().isUnauthorized());
        }

        // --- flagReview (cenários adicionais) ---
        @Test
        @WithMockUser(roles = "USER")
        void flagReview_shouldReturn400_whenInvalidInput() throws Exception {
                ContentFlagRequestDTO invalidFlag = new ContentFlagRequestDTO();
                invalidFlag.setFlagReason(""); // Razão vazia

                when(contentFlagService.flagReview(eq(reviewId), eq(userId), any(ContentFlagRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("Razão de sinalização inválida"));

                mockMvc.perform(post("/api/reviews/{reviewId}/flag", reviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidFlag))
                                .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUserPrincipal,
                                                null, authenticatedUserPrincipal.getAuthorities()))))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void flagReview_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(post("/api/reviews/{reviewId}/flag", reviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new ContentFlagRequestDTO("Razão"))))
                                .andExpect(status().isUnauthorized());
        }

        // --- exportAsPdf ---
        @Test
        @WithMockUser(roles = "USER")
        void exportAsPdf_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(get("/api/export/pdf"))
                                .andExpect(status().isForbidden());
        }

        @Test
        // Sem @WithMockUser
        void exportAsPdf_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/export/pdf"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void exportAsPdf_shouldReturnPdfContent() throws Exception {
                doAnswer(invocation -> {
                        HttpServletResponse response = invocation.getArgument(0);
                        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
                        response.setHeader("Content-Disposition", "attachment; filename=\"reviews.pdf\"");
                        response.getOutputStream().write("PDF Content".getBytes());
                        return null;
                }).when(reviewService).exportAsPdf(any(HttpServletResponse.class));

                mockMvc.perform(get("/api/export/pdf"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                                .andExpect(header().string("Content-Disposition",
                                                "attachment; filename=\"reviews.pdf\""))
                                .andExpect(content().string("PDF Content"));
        }

        // --- getUserStatistics ---
        @Test
        @WithMockUser(roles = "USER")
        void getUserStatistics_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(get("/api/reviews/{userId}/userStatistics", userId))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserStatistics_shouldReturn404_whenUserNotFound() throws Exception {
                when(reviewService.getUserStatistics(any(Pageable.class), eq(nonExistentId)))
                                .thenThrow(new ResourceNotFoundException("Usuário não encontrado para estatísticas"));
                mockMvc.perform(get("/api/reviews/{userId}/userStatistics", nonExistentId))
                                .andExpect(status().isNotFound());
        }

        @Test
        // Sem @WithMockUser
        void getUserStatistics_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/reviews/{userId}/userStatistics", userId))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserStatistics_shouldReturnCorrectStatistics_whenUserHasNoReviews() throws Exception {
                String emptyStats = "{}"; // Exemplo de JSON vazio
                when(reviewService.getUserStatistics(any(Pageable.class), eq(userId))).thenReturn(emptyStats);

                mockMvc.perform(get("/api/reviews/{userId}/userStatistics", userId))
                                .andExpect(status().isOk())
                                .andExpect(content().json(emptyStats));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserStatistics_shouldCalculateCorrectly_withVariousScores() throws Exception {
                ReviewAveragesDTO calculatedAverages = new ReviewAveragesDTO(4.5, 4.0, 5.0, 4.2);
                when(reviewService.getUserStatistics(any(Pageable.class), eq(userId)))
                                .thenReturn("Calculated Stats String"); // Ajustado para String

                mockMvc.perform(get("/api/reviews/{userId}/userStatistics", userId))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Calculated Stats String")); // Verifica o conteúdo da
                                                                                         // string
        }

        // --- getAverageWeighted ---
        @Test
        @WithMockUser(roles = "USER")
        void getAverageWeighted_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(get("/api/reviews/{userId}/average-weighted", userId))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAverageWeighted_shouldReturn404_whenMovieNotFound() throws Exception {
                when(reviewService.getAverageWeighted(any(Pageable.class), eq(nonExistentId)))
                                .thenThrow(new ResourceNotFoundException("Filme não encontrado para média ponderada"));
                mockMvc.perform(get("/api/reviews/{userId}/average-weighted", nonExistentId))
                                .andExpect(status().isNotFound());
        }

        @Test
        // Sem @WithMockUser
        void getAverageWeighted_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/reviews/{userId}/average-weighted", userId))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAverageWeighted_shouldReturnCorrectAverage_whenNoReviews() throws Exception {
                ReviewAveragesDTO emptyAverages = new ReviewAveragesDTO(0.0, 0.0, 0.0, 0.0);
                when(reviewService.getAverageWeighted(any(Pageable.class), eq(userId))).thenReturn(emptyAverages);

                mockMvc.perform(get("/api/reviews/{userId}/average-weighted", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.generalAverage").value(0.0));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAverageWeighted_shouldCalculateCorrectly_withVariousScores() throws Exception {
                ReviewAveragesDTO calculatedAverages = new ReviewAveragesDTO(4.0, 4.2, 5.0, 4.3);
                when(reviewService.getAverageWeighted(any(Pageable.class), eq(userId))).thenReturn(calculatedAverages);

                mockMvc.perform(get("/api/reviews/{userId}/average-weighted", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.generalAverage").value(4.3))
                                .andExpect(jsonPath("$.directionAverage").value(4.0))
                                .andExpect(jsonPath("$.screenplayAverage").value(4.2))
                                .andExpect(jsonPath("$.cinematographyAverage").value(5.0));
        }
}