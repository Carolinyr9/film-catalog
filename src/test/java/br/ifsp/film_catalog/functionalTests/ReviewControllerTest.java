package br.ifsp.film_catalog.functionalTests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.film_catalog.dto.ContentFlagRequestDTO;
import br.ifsp.film_catalog.dto.ReviewRequestDTO;
import br.ifsp.film_catalog.dto.ReviewResponseDTO;
import br.ifsp.film_catalog.exception.GlobalExceptionHandler;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.ReviewRepository;
import br.ifsp.film_catalog.repository.RoleRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.UserWatchedRepository;
import br.ifsp.film_catalog.service.ReviewService;
import br.ifsp.film_catalog.service.UserService;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)

class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserWatchedRepository userWatchedRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    private String token;
    private Long userId;
    private Long movieId2;
    private Long movieId3;
    private Long reviewId;
    private Long reviewId2;

    @BeforeEach
    void setUp() throws Exception {
        movieRepository.deleteAll();
        reviewRepository.deleteAll();

        Genre genre1 = new Genre("Drama");
        Genre genre2 = new Genre("Ação");

        User user = userRepository.findByUsername("user")
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        userId = user.getId();

        User user2 = userRepository.findByUsername("user2")
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId2 = user2.getId();

        Movie movie2 = new Movie();
        movie2.setTitle("Forrest Gump");
        movie2.setSynopsis("A vida de um homem com QI abaixo da média");
        movie2.setReleaseYear(1994);
        movie2.setDuration(142);
        movie2.setContentRating(ContentRating.A14);
        movie2.getGenres().add(genre1);
        movieRepository.save(movie2);
        Movie movie3 = new Movie();
        movie3.setTitle("A Origem");
        movie3.setSynopsis("Um ladrão que rouba segredos ao invadir sonhos");
        movie3.setReleaseYear(2010);
        movie3.setDuration(148);
        movie3.getGenres().add(genre2);
        movie3.getGenres().add(genre1);
        movie3.setContentRating(ContentRating.A12);
        movieRepository.save(movie3);
        
        movieId2 = movie2.getId();
        movieId3 = movie3.getId();

        userService.addWatchedMovie(userId, movieId2);
        ReviewResponseDTO reviewResponseDTO = reviewService.createReview(userId, movieId2, new ReviewRequestDTO("Excelente filme!", 5, 5, 5, 5));
        reviewId = reviewResponseDTO.getId();

        userService.addWatchedMovie(userId2, movieId2);
        reviewResponseDTO = reviewService.createReview(userId2, movieId2, new ReviewRequestDTO("Muito bom!", 4, 4, 4, 4));
        reviewId2 = reviewResponseDTO.getId();

        token = autenticarEObterToken();
    }

    private String autenticarEObterToken() throws Exception {
        String loginRequest = """
            {
                "username": "user",
                "password": "password"
            }
        """;

        MvcResult result = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = responseBody;

        return token;
    }

    private String loginADMIN() throws Exception {
        String loginRequest = """
            {
                "username": "admin",
                "password": "password"
            }
        """;

        MvcResult result = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = responseBody;

        return token;
    }

    @Test
    @DisplayName("Deve adicionar um filme assistido com sucesso")
    void deveAdicionarUmFilmeAssistidoComSucesso() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(post("/api/users/{userId}/watched/{movieId}", userId, movieId3)
                            .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve criar uma review com sucesso")
    void shouldCreateReviewSuccessfully() throws Exception {
        token = autenticarEObterToken();

        userService.addWatchedMovie(userId, movieId3);

        ReviewRequestDTO newReview = new ReviewRequestDTO();
        newReview.setContent("Um filme incrível com camadas de significado.");
        newReview.setDirectionScore(4);
        newReview.setScreenplayScore(5);
        newReview.setCinematographyScore(4);
        newReview.setGeneralScore(4);

        String reviewJson = objectMapper.writeValueAsString(newReview);

        mockMvc.perform(post("/api/users/{userId}/movies/{movieId}/reviews", userId, movieId3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Um filme incrível com camadas de significado."))
                .andExpect(jsonPath("$.directionScore").value(4))
                .andExpect(jsonPath("$.screenplayScore").value(5))
                .andExpect(jsonPath("$.cinematographyScore").value(4))
                .andExpect(jsonPath("$.generalScore").value(4));
    }

    @Test
    @DisplayName("Deve obter uma review por ID com sucesso")
    void deveObterUmaReviewPorIdComSucesso() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.content").value("Excelente filme!"))
                .andExpect(jsonPath("$.directionScore").value(5))
                .andExpect(jsonPath("$.screenplayScore").value(5))
                .andExpect(jsonPath("$.cinematographyScore").value(5))
                .andExpect(jsonPath("$.generalScore").value(5));
    }

    @Test
    @DisplayName("Deve obter todas as avaliações de um filme com sucesso")
    void deveObterTodasAsAvaliacoesDeUmFilmeComSucesso() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/movies/{movieId}/reviews", movieId2)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(reviewId))
                .andExpect(jsonPath("$.content[0].content").value("Excelente filme!"))
                .andExpect(jsonPath("$.content[0].directionScore").value(5))
                .andExpect(jsonPath("$.content[0].screenplayScore").value(5))
                .andExpect(jsonPath("$.content[0].cinematographyScore").value(5))
                .andExpect(jsonPath("$.content[0].generalScore").value(5));
    }

    @Test
    @DisplayName("Deve obter todas as avaliações feitas por um usuário com sucesso")
    void deveObterTodasAsAvaliacoesFeitasPorUmUsuarioComSucesso() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/users/{userId}/reviews", userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(reviewId))
                .andExpect(jsonPath("$.content[0].content").value("Excelente filme!"))
                .andExpect(jsonPath("$.content[0].directionScore").value(5))
                .andExpect(jsonPath("$.content[0].screenplayScore").value(5))
                .andExpect(jsonPath("$.content[0].cinematographyScore").value(5))
                .andExpect(jsonPath("$.content[0].generalScore").value(5));
    }

    @Test
    @DisplayName("Deve atualizar uma avaliação existente com sucesso")
    void deveAtualizarUmaAvaliacaoExistenteComSucesso() throws Exception {
        token = autenticarEObterToken();
        ReviewRequestDTO updatedReview = new ReviewRequestDTO();
        updatedReview.setContent("Uma obra-prima do cinema.");
        updatedReview.setDirectionScore(5);
        updatedReview.setScreenplayScore(5);
        updatedReview.setCinematographyScore(5);
        updatedReview.setGeneralScore(5);
        String reviewJson = objectMapper.writeValueAsString(updatedReview);
        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.content").value("Uma obra-prima do cinema."))
                .andExpect(jsonPath("$.directionScore").value(5))
                .andExpect(jsonPath("$.screenplayScore").value(5))
                .andExpect(jsonPath("$.cinematographyScore").value(5))
                .andExpect(jsonPath("$.generalScore").value(5));
    }

    @Test
    @DisplayName("Deve curtir uma avaliação com sucesso")
    void deveCurtirUmaAvaliacaoComSucesso() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(1));
    }
    
    @Test
    @DisplayName("Deve sinalizar uma avaliação com sucesso")
    void deveSinalizarUmaAvaliacaoComSucesso() throws Exception {
        token = autenticarEObterToken();
        ContentFlagRequestDTO flagRequest = new ContentFlagRequestDTO();
        flagRequest.setFlagReason("Conteúdo ofensivo");
        String flagJson = objectMapper.writeValueAsString(flagRequest);
        mockMvc.perform(post("/api/reviews/{reviewId}/flag", reviewId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flagJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(reviewId2))
                .andExpect(jsonPath("$.flagReason").value("Conteúdo ofensivo"));
    }

    @Test
    @DisplayName("Deve listar estatísticas de avaliações feitas por um usuário específico")
    void deveListarEstatisticasDeAvaliacoesFeitasPorUmUsuarioEspecifico() throws Exception {
        token = loginADMIN();
        mockMvc.perform(get("/api/reviews/{userId}/userStatistics", userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                //A resposta é dada como um texto String, então não é possível verificar os campos diretamente
                .andExpect(content().string(Matchers.not(Matchers.emptyString())));
    }

    @Test
    @DisplayName("Deve listar média ponderada das avaliações por critérios de um usuário específico")
    void deveListarMediaPonderadaDasAvaliacoesPorCriteriosDeUmUsuarioEspecifico() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/reviews/{userId}/average-weighted", userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(5.0))
                .andExpect(jsonPath("$[1]").value(5.0))
                .andExpect(jsonPath("$[2]").value(5.0))
                .andExpect(jsonPath("$[3]").value(5.0));
    }

    @Test
    @DisplayName("Deve deletar uma avaliação com sucesso")
    void deveDeletarUmaAvaliacaoComSucesso() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}

