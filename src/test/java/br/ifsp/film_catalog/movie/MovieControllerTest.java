package br.ifsp.film_catalog.movie;

import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.repository.GenreRepository;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.MovieService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    private MovieResponseDTO exampleMovie;

    @BeforeEach
    void setup() {
        exampleMovie = MovieResponseDTO.builder()
            .id(10L)
            .title("Exemplo de Filme")
            .synopsis("Descrição do filme de exemplo")
            .releaseYear(2020)
            .build();
    }

    @Test
    void getAllMovies_shouldReturnPagedMovies() throws Exception {
        PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                List.of(exampleMovie), 
                0,              
                10,                  
                1,            
                1,                 
                true               
        );

        when(movieService.getAllMovies(any())).thenReturn(pagedMovies);

        mockMvc.perform(get("/api/movies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(exampleMovie.getId()))
            .andExpect(jsonPath("$.content[0].title").value(exampleMovie.getTitle()));

        verify(movieService).getAllMovies(any());
    }

    @Test
    void getMovieById_shouldReturnMovie_whenExists() throws Exception {
        when(movieService.getMovieById(10L)).thenReturn(exampleMovie);

        mockMvc.perform(get("/api/movies/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(exampleMovie.getId()))
            .andExpect(jsonPath("$.title").value(exampleMovie.getTitle()));

        verify(movieService).getMovieById(10L);
    }

    @Test
    void getMoviesByTitle_shouldReturnPagedMovies() throws Exception {
        PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                List.of(exampleMovie), 
                0,              
                10,                  
                1,            
                1,                 
                true               
        );
        when(movieService.getMoviesByTitle(eq("exemplo"), any())).thenReturn(pagedMovies);

        mockMvc.perform(get("/api/movies/search/by-title")
                .param("title", "exemplo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value(exampleMovie.getTitle()));

        verify(movieService).getMoviesByTitle(eq("exemplo"), any());
    }

    @Test
    void getMoviesByGenre_shouldReturnPagedMovies() throws Exception {
        PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                List.of(exampleMovie), 
                0,              
                10,                  
                1,            
                1,                 
                true               
        );
        when(movieService.getMoviesByGenre(eq("ação"), any())).thenReturn(pagedMovies);

        mockMvc.perform(get("/api/movies/search/by-genre")
                .param("genreName", "ação"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value(exampleMovie.getTitle()));

        verify(movieService).getMoviesByGenre(eq("ação"), any());
    }

    @Test
    void getMoviesByReleaseYear_shouldReturnPagedMovies() throws Exception {
        PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                List.of(exampleMovie), 
                0,              
                10,                  
                1,            
                1,                 
                true               
        );
        when(movieService.getMoviesByReleaseYear(eq(2020), any())).thenReturn(pagedMovies);

        mockMvc.perform(get("/api/movies/search/by-year")
                .param("year", "2020"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].releaseYear").value(2020));

        verify(movieService).getMoviesByReleaseYear(eq(2020), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMovie_shouldReturnCreatedMovie_whenValid() throws Exception {
        MovieRequestDTO movieRequest = new MovieRequestDTO();
        movieRequest.setTitle("Novo Filme");
        movieRequest.setSynopsis("Descrição");
        movieRequest.setReleaseYear(2023);

        MovieResponseDTO createdMovie = MovieResponseDTO.builder()
            .id(20L)
            .title("Novo Filme")
            .synopsis("Descrição")
            .releaseYear(2023)
            .build();

        when(movieService.createMovie(any(MovieRequestDTO.class))).thenReturn(createdMovie);

        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(20L))
            .andExpect(jsonPath("$.title").value("Novo Filme"));

        verify(movieService).createMovie(any(MovieRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMovie_shouldReturnUpdatedMovie_whenValid() throws Exception {
        MovieRequestDTO movieRequest = new MovieRequestDTO();
        movieRequest.setTitle("Filme Atualizado");
        movieRequest.setSynopsis("Nova descrição");
        movieRequest.setReleaseYear(2022);

        MovieResponseDTO updatedMovie = MovieResponseDTO.builder()
            .id(10L)
            .title("Filme Atualizado")
            .synopsis("Nova descrição")
            .releaseYear(2022)
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
        void patchMovie_shouldReturnPatchedMovie_whenValid() throws Exception {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setSynopsis("Descrição parcial atualizada");

        MovieResponseDTO patchedMovie = MovieResponseDTO.builder()
                .id(10L)
                .title("Exemplo de Filme")
                .synopsis("Descrição parcial atualizada")
                .releaseYear(2020)
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
    void deleteMovie_shouldReturnNoContent_whenExists() throws Exception {
        doNothing().when(movieService).deleteMovie(10L);

        mockMvc.perform(delete("/api/movies/10"))
            .andExpect(status().isNoContent());

        verify(movieService).deleteMovie(10L);
    }

    @Test
        @WithMockUser 
        void getHighlightedMovies_shouldReturnPagedMovies() throws Exception {
        PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                List.of(exampleMovie), 
                0,              
                10,                  
                1,            
                1,                 
                true               
        );

        when(movieService.getHighlightedMovies(eq(0), eq(10))).thenReturn(pagedMovies);

        mockMvc.perform(get("/api/movies/highlighted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(exampleMovie.getId()));

        verify(movieService).getHighlightedMovies(eq(0), eq(10));
        }


}
