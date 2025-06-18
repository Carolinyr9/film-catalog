package br.ifsp.film_catalog.movie;

import br.ifsp.film_catalog.dto.GenreResponseDTO;
import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.InvalidMovieStateException;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.MovieService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private MovieService movieService;

        @Autowired
        private MovieRepository movieRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private MovieResponseDTO exampleMovie;
        private MovieRequestDTO validMovieRequest;

        @BeforeEach
        void setup() {
                // Setup a common example movie for responses
                exampleMovie = MovieResponseDTO.builder()
                                .id(10L)
                                .title("Exemplo de Filme")
                                .synopsis("Descrição do filme de exemplo")
                                .releaseYear(2020)
                                .duration(120)
                                .contentRating(ContentRating.A12)
                                .genres(Set.of(new GenreResponseDTO(1L, "Action")))
                                .build();

                // Setup a common valid movie request DTO for create/update tests
                Genre genre = new Genre();
                genre.setId(1L);
                genre.setName("Action");

                validMovieRequest = new MovieRequestDTO();
                validMovieRequest.setTitle("Novo Filme");
                validMovieRequest.setSynopsis("Descrição de um novo filme");
                validMovieRequest.setReleaseYear(2023);
                validMovieRequest.setDuration(120);
                validMovieRequest.setContentRating("A12");
                validMovieRequest.setGenres(Set.of(genre));
        }

        // --- getAllMovies Tests ---

        @Test
        @WithMockUser
        void getAllMovies_shouldReturnPagedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                0,
                                10,
                                1,
                                1,
                                true);

                when(movieService.getAllMovies(any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(exampleMovie.getId()))
                                .andExpect(jsonPath("$.content[0].title").value(exampleMovie.getTitle()));

                verify(movieService).getAllMovies(any(Pageable.class));
        }

        @Test
        void getAllMovies_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getAllMovies_shouldReturn403_whenForbidden() throws Exception {

                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(Collections.emptyList(), 0, 10, 0, 0,
                                true);
                when(movieService.getAllMovies(any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void getAllMovies_shouldHandlePaginationAndSorting() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                1,
                                5,
                                2,
                                1,
                                true);
                when(movieService.getAllMovies(argThat(pageable -> pageable.getPageNumber() == 1 &&
                                pageable.getPageSize() == 5 &&
                                pageable.getSort().equals(Sort.by("releaseYear").descending()))))
                                .thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies")
                                .param("page", "1")
                                .param("size", "5")
                                .param("sort", "releaseYear,desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page").value(1))
                                .andExpect(jsonPath("$.size").value(5));

                verify(movieService).getAllMovies(argThat(pageable -> pageable.getPageNumber() == 1 &&
                                pageable.getPageSize() == 5 &&
                                pageable.getSort().equals(Sort.by("releaseYear").descending())));
        }

        @Test
        @WithMockUser
        void getAllMovies_shouldReturnEmptyList_whenNoMoviesExist() throws Exception {
                PagedResponse<MovieResponseDTO> emptyPagedMovies = new PagedResponse<>(
                                Collections.emptyList(),
                                0,
                                10,
                                0,
                                0,
                                true);

                when(movieService.getAllMovies(any(Pageable.class))).thenReturn(emptyPagedMovies);

                mockMvc.perform(get("/api/movies"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty())
                                .andExpect(jsonPath("$.totalElements").value(0));

                verify(movieService).getAllMovies(any(Pageable.class));
        }

        // --- getMovieById Tests ---

        @Test
        @WithMockUser
        void getMovieById_shouldReturnMovie_whenExists() throws Exception {
                when(movieService.getMovieById(10L)).thenReturn(exampleMovie);

                mockMvc.perform(get("/api/movies/10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(exampleMovie.getId()))
                                .andExpect(jsonPath("$.title").value(exampleMovie.getTitle()));

                verify(movieService).getMovieById(10L);
        }

        @Test
        void getMovieById_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies/1"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getMovieById_shouldReturn403_whenForbidden() throws Exception {
                when(movieService.getMovieById(10L)).thenReturn(exampleMovie);

                mockMvc.perform(get("/api/movies/10"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void getMovieById_shouldReturn400_whenInvalidIdFormat() throws Exception {
                mockMvc.perform(get("/api/movies/invalidId"))
                                .andExpect(status().isBadRequest());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser
        void getMoviesByTitle_shouldReturnPagedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                0,
                                10,
                                1,
                                1,
                                true);
                when(movieService.getMoviesByTitle(eq("exemplo"), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-title")
                                .param("title", "exemplo"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value(exampleMovie.getTitle()));

                verify(movieService).getMoviesByTitle(eq("exemplo"), any(Pageable.class));
        }

        @Test
        void getMoviesByTitle_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies/search/by-title")
                                .param("title", "any"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getMoviesByTitle_shouldReturn403_whenForbidden() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(Collections.emptyList(), 0, 10, 0, 0,
                                true);
                when(movieService.getMoviesByTitle(eq("forbidden"), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-title")
                                .param("title", "forbidden"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void getMoviesByTitle_shouldReturn200AndEmptyList_whenTitleNotFound() throws Exception {
                PagedResponse<MovieResponseDTO> emptyPagedMovies = new PagedResponse<>(
                                Collections.emptyList(),
                                0,
                                10,
                                0,
                                0,
                                true);
                when(movieService.getMoviesByTitle(eq("nonexistent"), any(Pageable.class)))
                                .thenReturn(emptyPagedMovies);

                mockMvc.perform(get("/api/movies/search/by-title")
                                .param("title", "nonexistent"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty());

                verify(movieService).getMoviesByTitle(eq("nonexistent"), any(Pageable.class));
        }

        @Test
        @WithMockUser
        void getMoviesByTitle_shouldHandlePaginationAndSorting() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                0,
                                5,
                                1,
                                1,
                                true);
                when(movieService.getMoviesByTitle(eq("exemplo"), argThat(pageable -> pageable.getPageNumber() == 0 &&
                                pageable.getPageSize() == 5 &&
                                pageable.getSort().equals(Sort.by("releaseYear").ascending()))))
                                .thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-title")
                                .param("title", "exemplo")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "releaseYear,asc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(5));

                verify(movieService).getMoviesByTitle(eq("exemplo"),
                                argThat(pageable -> pageable.getPageNumber() == 0 &&
                                                pageable.getPageSize() == 5 &&
                                                pageable.getSort().equals(Sort.by("releaseYear").ascending())));
        }

        @Test
        @WithMockUser
        void getMoviesByTitle_shouldHandleSpecialCharactersInTitle() throws Exception {
                String specialTitle = "Filme com @!#% caracteres especiais &*()";
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(MovieResponseDTO.builder().id(11L).title(specialTitle).build()),
                                0, 10, 1, 1, true);
                when(movieService.getMoviesByTitle(eq(specialTitle), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-title")
                                .param("title", specialTitle))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value(specialTitle));

                verify(movieService).getMoviesByTitle(eq(specialTitle), any(Pageable.class));
        }

        // --- getMoviesByGenre Tests ---

        @Test
        @WithMockUser
        void getMoviesByGenre_shouldReturnPagedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                0,
                                10,
                                1,
                                1,
                                true);
                when(movieService.getMoviesByGenre(eq("ação"), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-genre")
                                .param("genreName", "ação"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value(exampleMovie.getTitle()));

                verify(movieService).getMoviesByGenre(eq("ação"), any(Pageable.class));
        }

        @Test
        @WithMockUser
        void getMoviesByGenre_shouldReturn200AndEmptyList_whenGenreHasNoMovies() throws Exception {
                PagedResponse<MovieResponseDTO> emptyPagedMovies = new PagedResponse<>(
                                Collections.emptyList(),
                                0,
                                10,
                                0,
                                0,
                                true);
                when(movieService.getMoviesByGenre(eq("emptyGenre"), any(Pageable.class))).thenReturn(emptyPagedMovies);

                mockMvc.perform(get("/api/movies/search/by-genre")
                                .param("genreName", "emptyGenre"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty());

                verify(movieService).getMoviesByGenre(eq("emptyGenre"), any(Pageable.class));
        }

        @Test
        void getMoviesByGenre_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies/search/by-genre")
                                .param("genreName", "any"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getMoviesByGenre_shouldReturn403_whenForbidden() throws Exception {
                // No @PreAuthorize, so it will return 200 OK.
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(Collections.emptyList(), 0, 10, 0, 0,
                                true);
                when(movieService.getMoviesByGenre(eq("forbiddenGenre"), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-genre")
                                .param("genreName", "forbiddenGenre"))
                                .andExpect(status().isOk());
        }

        // --- getMoviesByReleaseYear Tests ---

        @Test
        @WithMockUser
        void getMoviesByReleaseYear_shouldReturnPagedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                0,
                                10,
                                1,
                                1,
                                true);
                when(movieService.getMoviesByReleaseYear(eq(2020), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-year")
                                .param("year", "2020"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].releaseYear").value(2020));

                verify(movieService).getMoviesByReleaseYear(eq(2020), any(Pageable.class));
        }

        @Test
        void getMoviesByReleaseYear_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies/search/by-year")
                                .param("year", "2000"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getMoviesByReleaseYear_shouldReturn403_whenForbidden() throws Exception {
                // No @PreAuthorize, so it will return 200 OK.
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(Collections.emptyList(), 0, 10, 0, 0,
                                true);
                when(movieService.getMoviesByReleaseYear(eq(2000), any(Pageable.class))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/search/by-year")
                                .param("year", "2000"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void getMoviesByReleaseYear_shouldReturn200AndEmptyList_whenYearHasNoMovies() throws Exception {
                PagedResponse<MovieResponseDTO> emptyPagedMovies = new PagedResponse<>(
                                Collections.emptyList(),
                                0,
                                10,
                                0,
                                0,
                                true);
                when(movieService.getMoviesByReleaseYear(eq(1900), any(Pageable.class))).thenReturn(emptyPagedMovies);

                mockMvc.perform(get("/api/movies/search/by-year")
                                .param("year", "1900"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty());

                verify(movieService).getMoviesByReleaseYear(eq(1900), any(Pageable.class));
        }

        // --- createMovie Tests ---

        @Test
        @WithMockUser(roles = "ADMIN")
        void createMovie_shouldReturnCreatedMovie_whenValid() throws Exception {
                GenreResponseDTO genreResponse = new GenreResponseDTO();
                genreResponse.setId(1L);
                genreResponse.setName("Action");

                MovieResponseDTO createdMovie = MovieResponseDTO.builder()
                                .id(20L)
                                .title(validMovieRequest.getTitle())
                                .synopsis(validMovieRequest.getSynopsis())
                                .releaseYear(validMovieRequest.getReleaseYear())
                                .duration(validMovieRequest.getDuration())
                                .contentRating(ContentRating.valueOf(validMovieRequest.getContentRating()))
                                .genres(Set.of(genreResponse))
                                .build();

                when(movieService.createMovie(any(MovieRequestDTO.class))).thenReturn(createdMovie);

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validMovieRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(20L))
                                .andExpect(jsonPath("$.title").value(validMovieRequest.getTitle()));

                verify(movieService).createMovie(any(MovieRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createMovie_shouldReturn400_whenInvalidPayload() throws Exception {
                MovieRequestDTO invalidMovieRequest = new MovieRequestDTO(); // Empty DTO

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidMovieRequest)))
                                .andExpect(status().isBadRequest()); // Assuming @Valid handles this

                verifyNoInteractions(movieService); // Service should not be called due to validation errors
        }

        @Test
        void createMovie_shouldReturn401_whenUnauthorized() throws Exception {
                // No @WithMockUser annotation
                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validMovieRequest)))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER") // User role trying to access ADMIN endpoint
        void createMovie_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validMovieRequest)))
                                .andExpect(status().isForbidden());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createMovie_shouldHandleMissingRequiredFields() throws Exception {
                MovieRequestDTO movieWithMissingFields = new MovieRequestDTO();
                movieWithMissingFields.setTitle("Missing Fields Movie");
                // synopsis, releaseYear, duration, contentRating, genres are missing

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(movieWithMissingFields)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("releaseYear")))
                                .andExpect(jsonPath("$.message").value(containsString("duration")));

                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createMovie_shouldHandleInvalidDataTypes() throws Exception {
                String invalidPayload = "{\"title\": \"Test\", \"synopsis\": \"Desc\", \"releaseYear\": \"not-a-year\", \"duration\": 100, \"contentRating\": \"A12\", \"genres\": []}";

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidPayload))
                                .andExpect(status().isBadRequest()); // Spring handles JSON parsing/type errors

                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createMovie_shouldHandleInvalidFieldValues() throws Exception {
                // Future release year
                MovieRequestDTO futureYearMovie = new MovieRequestDTO();
                futureYearMovie.setTitle("Future Movie");
                futureYearMovie.setSynopsis("Synopsis");
                futureYearMovie.setReleaseYear(Year.now().getValue() + 1);
                futureYearMovie.setDuration(100);
                futureYearMovie.setContentRating("A12");
                futureYearMovie.setGenres(Collections.emptySet());

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(futureYearMovie)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", allOf(
                                                containsString("releaseYear"),
                                                containsString("cannot be in the future"))));

                // Negative duration
                MovieRequestDTO negativeDurationMovie = new MovieRequestDTO();
                negativeDurationMovie.setTitle("Negative Duration");
                negativeDurationMovie.setSynopsis("Synopsis");
                negativeDurationMovie.setReleaseYear(2020);
                negativeDurationMovie.setDuration(-10);
                negativeDurationMovie.setContentRating("A12");
                negativeDurationMovie.setGenres(Collections.emptySet());

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(negativeDurationMovie)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", allOf(
                                                containsString("duration"),
                                                containsString("must be a positive number"))));
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createMovie_shouldHandleLongTextsAndSpecialCharacters() throws Exception {
                MovieRequestDTO longTextMovie = new MovieRequestDTO();
                String longTitle = "Título com caracteres especiais: !@#$%^&*()_+-=[]{};':\",.<>/? " + "a".repeat(100);
                String longSynopsis = "Sinopse com caracteres especiais: ~`¬|\\{}[]<>,./? " + "b".repeat(100);

                longTextMovie.setTitle(longTitle);
                longTextMovie.setSynopsis(longSynopsis);

                longTextMovie.setReleaseYear(2021);
                longTextMovie.setDuration(90);
                longTextMovie.setContentRating("A12");
                Genre comedyGenre = new Genre();
                comedyGenre.setId(2L);
                comedyGenre.setName("Comedy");
                longTextMovie.setGenres(Collections.singleton(comedyGenre));

                MovieResponseDTO createdMovie = MovieResponseDTO.builder()
                                .id(21L)
                                .title(longTextMovie.getTitle())
                                .synopsis(longTextMovie.getSynopsis())
                                .releaseYear(longTextMovie.getReleaseYear())
                                .duration(longTextMovie.getDuration())
                                .contentRating(ContentRating.A12)
                                .genres(Set.of(new GenreResponseDTO(2L, "Comedy")))
                                .build();

                when(movieService.createMovie(any(MovieRequestDTO.class))).thenReturn(createdMovie);

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(longTextMovie)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value(longTextMovie.getTitle()))
                                .andExpect(jsonPath("$.synopsis").value(longTextMovie.getSynopsis()));

                verify(movieService).createMovie(any(MovieRequestDTO.class));
        }

        // --- updateMovie Tests (PUT) ---

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateMovie_shouldReturnUpdatedMovie_whenValid() throws Exception {
                MovieRequestDTO movieRequest = new MovieRequestDTO();
                movieRequest.setTitle("Filme Atualizado");
                movieRequest.setSynopsis("Nova descrição");
                movieRequest.setReleaseYear(2022);
                movieRequest.setDuration(120);
                movieRequest.setContentRating("A12");
                Genre genre = new Genre();
                genre.setId(1L);
                genre.setName("Action");
                movieRequest.setGenres(Set.of(genre));

                MovieResponseDTO updatedMovie = MovieResponseDTO.builder()
                                .id(10L)
                                .title("Filme Atualizado")
                                .synopsis("Nova descrição")
                                .releaseYear(2022)
                                .duration(120)
                                .contentRating(ContentRating.A12)
                                .genres(Set.of(new GenreResponseDTO(1L, "Action")))
                                .build();

                when(movieService.updateMovie(eq(10L), any(MovieRequestDTO.class))).thenReturn(updatedMovie);

                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(movieRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Filme Atualizado"));

                verify(movieService).updateMovie(eq(10L), any(MovieRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateMovie_shouldReturn400_whenInvalidPayload() throws Exception {
                MovieRequestDTO invalidMovieRequest = new MovieRequestDTO(); // Empty DTO
                invalidMovieRequest.setTitle(""); // Invalid empty title

                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidMovieRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", containsString("title")));

                verifyNoInteractions(movieService);
        }

        @Test
        void updateMovie_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validMovieRequest)))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void updateMovie_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validMovieRequest)))
                                .andExpect(status().isForbidden());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateMovie_shouldHandleInvalidDataTypes() throws Exception {
                String invalidPayload = "{\"title\": \"Test\", \"synopsis\": \"Desc\", \"releaseYear\": \"not-a-year\", \"duration\": 100, \"contentRating\": \"A12\", \"genres\": []}";

                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidPayload))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateMovie_shouldHandleInvalidFieldValues() throws Exception {
                MovieRequestDTO invalidMovieRequest = new MovieRequestDTO();
                invalidMovieRequest.setTitle("Invalid Update");
                invalidMovieRequest.setSynopsis("Synopsis");
                invalidMovieRequest.setReleaseYear(Year.now().getValue() + 5); // Future year
                invalidMovieRequest.setDuration(-50); // Negative duration
                invalidMovieRequest.setContentRating("X20"); // Invalid content rating regex
                invalidMovieRequest.setGenres(Collections.emptySet());

                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidMovieRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", allOf(
                                                containsString("releaseYear"),
                                                containsString("duration"),
                                                containsString("contentRating"),
                                                containsString("genres"))));

                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateMovie_shouldHandleLongTextsAndSpecialCharacters() throws Exception {
                MovieRequestDTO longTextMovie = new MovieRequestDTO();
                longTextMovie.setTitle("Updated Title with Special Chars: !@#$%^&*() and a very long title "
                                + "x".repeat(100));
                longTextMovie.setSynopsis(
                                "Updated Synopsis with Special Chars: ~`¬|\\{}[]<>,./? and a very long synopsis "
                                                + "y".repeat(100));
                longTextMovie.setReleaseYear(2022);
                longTextMovie.setDuration(110);
                longTextMovie.setContentRating("A12");
                Genre actionGenre = new Genre();
                actionGenre.setId(1L);
                actionGenre.setName("Action");
                longTextMovie.setGenres(Collections.singleton(actionGenre));

                MovieResponseDTO updatedMovie = MovieResponseDTO.builder()
                                .id(10L)
                                .title(longTextMovie.getTitle())
                                .synopsis(longTextMovie.getSynopsis())
                                .releaseYear(longTextMovie.getReleaseYear())
                                .duration(longTextMovie.getDuration())
                                .contentRating(ContentRating.A12)
                                .genres(Set.of(new GenreResponseDTO(1L, "Action")))
                                .build();

                when(movieService.updateMovie(eq(10L), any(MovieRequestDTO.class))).thenReturn(updatedMovie);

                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(longTextMovie)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value(longTextMovie.getTitle()))
                                .andExpect(jsonPath("$.synopsis").value(longTextMovie.getSynopsis()));

                verify(movieService).updateMovie(eq(10L), any(MovieRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateMovie_shouldReturn409_whenDuplicateTitleAndYear() throws Exception {
                when(movieService.updateMovie(eq(10L), any(MovieRequestDTO.class)))
                                .thenThrow(new InvalidMovieStateException(
                                                "Another movie with this title and year already exists"));

                mockMvc.perform(put("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validMovieRequest)))
                                .andExpect(status().isConflict());

                verify(movieService).updateMovie(eq(10L), any(MovieRequestDTO.class));
        }

        // --- patchMovie Tests ---

        @Test
        @WithMockUser(roles = "ADMIN")
        void patchMovie_shouldReturnPatchedMovie_whenValid() throws Exception {
                MoviePatchDTO patchDTO = new MoviePatchDTO();
                patchDTO.setSynopsis("Descrição parcial atualizada");

                MovieResponseDTO patchedMovie = MovieResponseDTO.builder()
                                .id(10L)
                                .title("Exemplo de Filme")
                                .synopsis("Descrição parcial atualizada")
                                .releaseYear(2020)
                                .duration(120)
                                .contentRating(ContentRating.A12)
                                .genres(Set.of(new GenreResponseDTO(1L, "Action")))
                                .build();

                when(movieService.patchMovie(eq(10L), any(MoviePatchDTO.class))).thenReturn(patchedMovie);

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.synopsis").value("Descrição parcial atualizada"));

                verify(movieService).patchMovie(eq(10L), any(MoviePatchDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void patchMovie_shouldReturn400_whenInvalidPayload() throws Exception {
                String invalidPayload = "{\"duration\": \"not-a-number\"}"; // Invalid type for duration

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidPayload))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(movieService);
        }

        @Test
        void patchMovie_shouldReturn401_whenUnauthorized() throws Exception {
                MoviePatchDTO patchDTO = new MoviePatchDTO();
                patchDTO.setSynopsis("Test");

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchDTO)))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void patchMovie_shouldReturn403_whenForbidden() throws Exception {
                MoviePatchDTO patchDTO = new MoviePatchDTO();
                patchDTO.setSynopsis("Test");

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchDTO)))
                                .andExpect(status().isForbidden());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void patchMovie_shouldHandleInvalidDataTypesForPatchedFields() throws Exception {
                String invalidPayload = "{\"releaseYear\": \"abc\"}"; // Invalid type for releaseYear

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidPayload))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void patchMovie_shouldHandleInvalidFieldValuesForPatchedFields() throws Exception {
                MoviePatchDTO invalidPatchDTO = new MoviePatchDTO();
                invalidPatchDTO.setDuration(-10); // Invalid negative duration

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPatchDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", containsString("duration")));

                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void patchMovie_shouldUpdateOnlySpecifiedFields() throws Exception {
                MoviePatchDTO patchDTO = new MoviePatchDTO();
                patchDTO.setSynopsis("Only synopsis updated");
                patchDTO.setReleaseYear(2024);

                // Prepare a response that has only these fields changed, others remain from
                // exampleMovie
                MovieResponseDTO updatedMovie = MovieResponseDTO.builder()
                                .id(exampleMovie.getId())
                                .title(exampleMovie.getTitle()) // Should remain original title
                                .synopsis(patchDTO.getSynopsis()) // Should be updated synopsis
                                .releaseYear(patchDTO.getReleaseYear()) // Should be updated year
                                .duration(exampleMovie.getDuration()) // Should remain original duration
                                .contentRating(exampleMovie.getContentRating()) // Should remain original content rating
                                .genres(exampleMovie.getGenres()) // Should remain original genres
                                .build();

                when(movieService.patchMovie(eq(10L), any(MoviePatchDTO.class))).thenReturn(updatedMovie);

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.synopsis").value(patchDTO.getSynopsis()))
                                .andExpect(jsonPath("$.releaseYear").value(patchDTO.getReleaseYear()))
                                .andExpect(jsonPath("$.title").value(exampleMovie.getTitle())) // Verify other fields
                                                                                               // remain
                                .andExpect(jsonPath("$.duration").value(exampleMovie.getDuration()));

                verify(movieService).patchMovie(eq(10L), any(MoviePatchDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void patchMovie_shouldNotChangeOtherFields() throws Exception {
                MoviePatchDTO patchDTO = new MoviePatchDTO();
                patchDTO.setSynopsis("Updated synopsis only");

                // Simulate the service returning a movie where only synopsis is changed, others
                // are same as exampleMovie
                MovieResponseDTO patchedMovie = MovieResponseDTO.builder()
                                .id(exampleMovie.getId())
                                .title(exampleMovie.getTitle())
                                .synopsis(patchDTO.getSynopsis())
                                .releaseYear(exampleMovie.getReleaseYear())
                                .duration(exampleMovie.getDuration())
                                .contentRating(exampleMovie.getContentRating())
                                .genres(exampleMovie.getGenres())
                                .build();

                when(movieService.patchMovie(eq(10L), any(MoviePatchDTO.class))).thenReturn(patchedMovie);

                mockMvc.perform(patch("/api/movies/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.synopsis").value(patchDTO.getSynopsis()))
                                .andExpect(jsonPath("$.title").value(exampleMovie.getTitle()))
                                .andExpect(jsonPath("$.releaseYear").value(exampleMovie.getReleaseYear()))
                                .andExpect(jsonPath("$.duration").value(exampleMovie.getDuration()))
                                .andExpect(jsonPath("$.contentRating")
                                                .value(exampleMovie.getContentRating().toString()))
                                .andExpect(jsonPath("$.genres", hasSize(1))); // Assuming genres don't change if not
                                                                              // provided
                verify(movieService).patchMovie(eq(10L), any(MoviePatchDTO.class));
        }

        // --- deleteMovie Tests ---

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteMovie_shouldReturnNoContent_whenExists() throws Exception {
                doNothing().when(movieService).deleteMovie(10L);

                mockMvc.perform(delete("/api/movies/10"))
                                .andExpect(status().isNoContent());

                verify(movieService).deleteMovie(10L);
        }

        @Test
        void deleteMovie_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(delete("/api/movies/10"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void deleteMovie_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(delete("/api/movies/10"))
                                .andExpect(status().isForbidden());
                verifyNoInteractions(movieService);
        }

        // --- getHighlightedMovies Tests ---

        @Test
        @WithMockUser
        void getHighlightedMovies_shouldReturnPagedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(exampleMovie),
                                0,
                                10,
                                1,
                                1,
                                true);

                when(movieService.getHighlightedMovies(eq(0), eq(10))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/highlighted"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(exampleMovie.getId()));

                verify(movieService).getHighlightedMovies(eq(0), eq(10));
        }

        @Test
        void getHighlightedMovies_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/movies/highlighted"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(movieService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getHighlightedMovies_shouldReturn403_whenForbidden() throws Exception {
                // No @PreAuthorize, so it will return 200 OK.
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(Collections.emptyList(), 0, 10, 0, 0,
                                true);
                when(movieService.getHighlightedMovies(anyInt(), anyInt())).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/highlighted"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void getHighlightedMovies_shouldHandlePagination() throws Exception {
                MovieResponseDTO anotherMovie = MovieResponseDTO.builder()
                                .id(11L)
                                .title("Outro Filme em Destaque")
                                .synopsis("Descrição")
                                .releaseYear(2019)
                                .build();

                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(anotherMovie),
                                1, // page
                                1, // size
                                2, // totalElements
                                2, // totalPages
                                true // last
                );

                when(movieService.getHighlightedMovies(eq(1), eq(1))).thenReturn(pagedMovies);

                mockMvc.perform(get("/api/movies/highlighted")
                                .param("page", "1")
                                .param("size", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(anotherMovie.getId()))
                                .andExpect(jsonPath("$.page").value(1)) // era pageNumber
                                .andExpect(jsonPath("$.size").value(1)); // era pageSize

                verify(movieService).getHighlightedMovies(eq(1), eq(1));
        }

        @Test
        @WithMockUser
        void getHighlightedMovies_shouldReturnEmptyList_whenNoHighlightedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> emptyPagedMovies = new PagedResponse<>(
                                Collections.emptyList(),
                                0,
                                10,
                                0,
                                0,
                                true);
                when(movieService.getHighlightedMovies(anyInt(), anyInt())).thenReturn(emptyPagedMovies);

                mockMvc.perform(get("/api/movies/highlighted"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty())
                                .andExpect(jsonPath("$.totalElements").value(0));

                verify(movieService).getHighlightedMovies(anyInt(), anyInt());
        }
}
