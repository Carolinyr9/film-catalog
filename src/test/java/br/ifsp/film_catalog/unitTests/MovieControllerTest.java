package br.ifsp.film_catalog.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.ifsp.film_catalog.controller.MovieController;
import br.ifsp.film_catalog.dto.GenreResponseDTO;
import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.repository.GenreRepository;
import br.ifsp.film_catalog.service.MovieService;
import jakarta.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    @InjectMocks
    private MovieController movieController;

    @Mock
    private MovieService movieService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private GenreRepository genreRepository;

    @Test
    void testGetAllMovies_ReturnPagedMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Oppenheimer");
        movie1.setSynopsis("Biografia de um físico");
        movie1.setReleaseYear(2023);
        movie1.setDuration(180);
        movie1.setContentRating(ContentRating.A14); 
        movie1.getGenres().add(new Genre("Drama"));
        movie1.getGenres().add(new Genre("Histórico"));
        Movie movie2 = new Movie();
        movie2.setId(2L); 
        movie2.setTitle("Barbie");
        movie2.setSynopsis("Aventura em Barbielândia");
        movie2.setReleaseYear(2023);
        movie2.setDuration(115);
        movie2.setContentRating(ContentRating.AL);
        movie2.getGenres().add(new Genre("Aventura")); 
        movie2.getGenres().add(new Genre("Comédia"));

        MovieResponseDTO dto1 = new MovieResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Oppenheimer");
        dto1.setSynopsis("Biografia de um físico");
        dto1.setReleaseYear(2023);
        dto1.setContentRating(ContentRating.A14); 
        dto1.setDuration(180);
        dto1.getGenres().add(new GenreResponseDTO(1L,"Drama"));
        dto1.getGenres().add(new GenreResponseDTO(2L,"Histórico"));

        MovieResponseDTO dto2 = new MovieResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Barbie");
        dto2.setSynopsis("Aventura em Barbielândia");
        dto2.setReleaseYear(2023);
        dto2.setContentRating(ContentRating.AL);
        dto2.setDuration(115);
        dto2.getGenres().add(new GenreResponseDTO(3L,"Aventura"));
        dto2.getGenres().add(new GenreResponseDTO(4L,"Comédia"));

        PagedResponse<MovieResponseDTO> pagedResponse = new PagedResponse<>(
            List.of(dto1, dto2),
            0, 2, 2L, 1, true
        );

        when(movieService.getAllMovies(pageable)).thenReturn(pagedResponse);

        // When
        ResponseEntity<PagedResponse<MovieResponseDTO>> response = movieController.getAllMovies(pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getContent().get(0).getId());
        assertEquals(2L, response.getBody().getContent().get(1).getId());
    }

    @Test
    void testGetMovieById_ReturnMovie() {
    // Given
        Long movieId = 1L;

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Oppenheimer");
        movie.setSynopsis("Biografia de um físico");
        movie.setReleaseYear(2023);
        movie.setDuration(180);
        movie.setContentRating(ContentRating.A14);
        movie.getGenres().add(new Genre("Drama"));
        movie.getGenres().add(new Genre("Histórico"));

        MovieResponseDTO dto = new MovieResponseDTO();
        dto.setId(movieId);
        dto.setTitle("Oppenheimer");
        dto.setSynopsis("Biografia de um físico");
        dto.setReleaseYear(2023);
        dto.setDuration(180);
        dto.setContentRating(ContentRating.A14);
        dto.getGenres().add(new GenreResponseDTO(1L, "Drama"));
        dto.getGenres().add(new GenreResponseDTO(2L, "Histórico"));

        when(movieService.getMovieById(movieId)).thenReturn(dto);

        // When
        ResponseEntity<MovieResponseDTO> response = movieController.getMovieById(movieId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(movieId, response.getBody().getId());
        assertEquals("Oppenheimer", response.getBody().getTitle());
    }

    @Test
    void testGetMovieById_ReturnNotFound() {
        // Given
        Long invalidId = 999L;
        when(movieService.getMovieById(invalidId)).thenThrow(new ResourceNotFoundException("Filme não encontrado"));

        // When / Then
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            movieController.getMovieById(invalidId);
        });

        assertEquals("Filme não encontrado", thrown.getMessage());
    }
    
    @Test
    void testGetMoviesByTitle_ReturnPagedMovies() {
        // Given
        String searchTitle = "bar";
        Pageable pageable = PageRequest.of(0, 2);

        MovieResponseDTO dto1 = new MovieResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Barbie");
        dto1.setSynopsis("Aventura em Barbielândia");
        dto1.setReleaseYear(2023);
        dto1.setDuration(115);
        dto1.setContentRating(ContentRating.AL);
        dto1.getGenres().add(new GenreResponseDTO(1L, "Aventura"));
        dto1.getGenres().add(new GenreResponseDTO(2L, "Comédia"));

        MovieResponseDTO dto2 = new MovieResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Bar do Zé");
        dto2.setSynopsis("História sobre um bar famoso.");
        dto2.setReleaseYear(2022);
        dto2.setDuration(100);
        dto2.setContentRating(ContentRating.A12);
        dto2.getGenres().add(new GenreResponseDTO(3L, "Drama"));

        PagedResponse<MovieResponseDTO> pagedResponse = new PagedResponse<>(
            List.of(dto1, dto2),
            0, 2, 2L, 1, true
        );

        when(movieService.getMoviesByTitle(searchTitle, pageable)).thenReturn(pagedResponse);

        // When
        ResponseEntity<PagedResponse<MovieResponseDTO>> response = movieController.getMoviesByTitle(searchTitle, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Barbie", response.getBody().getContent().get(0).getTitle());
        assertEquals("Bar do Zé", response.getBody().getContent().get(1).getTitle());
    }

    @Test
    void testGetMoviesByTitle_EmptyResult() {
        // Given
        String searchTitle = "inexistente";
        Pageable pageable = PageRequest.of(0, 10);

        PagedResponse<MovieResponseDTO> emptyResponse = new PagedResponse<>(
            List.of(), 0, 0, 0L, 0, true
        );

        when(movieService.getMoviesByTitle(searchTitle, pageable)).thenReturn(emptyResponse);

        // When
        ResponseEntity<PagedResponse<MovieResponseDTO>> response = movieController.getMoviesByTitle(searchTitle, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());
    }

    @Test
    void testGetMoviesByGenre_ReturnPagedMovies() {
        // Given
        String genreName = "Drama";
        Pageable pageable = PageRequest.of(0, 2);

        MovieResponseDTO dto1 = new MovieResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Oppenheimer");
        dto1.setSynopsis("Biografia de um físico");
        dto1.setReleaseYear(2023);
        dto1.setDuration(180);
        dto1.setContentRating(ContentRating.A14);
        dto1.getGenres().add(new GenreResponseDTO(1L, "Drama"));

        MovieResponseDTO dto2 = new MovieResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Sociedade dos Poetas Mortos");
        dto2.setSynopsis("Inspiração e rebeldia em uma escola tradicional.");
        dto2.setReleaseYear(1989);
        dto2.setDuration(128);
        dto2.setContentRating(ContentRating.A12);
        dto2.getGenres().add(new GenreResponseDTO(1L, "Drama"));

        PagedResponse<MovieResponseDTO> pagedResponse = new PagedResponse<>(
            List.of(dto1, dto2),
            0, 2, 2L, 1, true
        );

        when(movieService.getMoviesByGenre(genreName, pageable)).thenReturn(pagedResponse);

        // When
        ResponseEntity<PagedResponse<MovieResponseDTO>> response = movieController.getMoviesByGenre(genreName, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Oppenheimer", response.getBody().getContent().get(0).getTitle());
        assertTrue(response.getBody().getContent().get(0).getGenres()
                    .contains(new GenreResponseDTO(1L, "Drama")));

    }
    @Test
    void testGetMoviesByGenre_GenreNotFound() {
        // Given
        String genreName = "GêneroInexistente";
        Pageable pageable = PageRequest.of(0, 10);

        when(movieService.getMoviesByGenre(genreName, pageable))
            .thenThrow(new ResourceNotFoundException("Gênero não encontrado"));

        // When / Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            movieController.getMoviesByGenre(genreName, pageable);
        });

        assertEquals("Gênero não encontrado", exception.getMessage());
    }

    @Test
    void testGetMoviesByReleaseYear_ReturnPagedMovies() {
        // Given
        int year = 2023;
        Pageable pageable = PageRequest.of(0, 2);

        MovieResponseDTO dto1 = new MovieResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Oppenheimer");
        dto1.setSynopsis("Biografia de um físico");
        dto1.setReleaseYear(2023);
        dto1.setDuration(180);
        dto1.setContentRating(ContentRating.A14);
        dto1.getGenres().add(new GenreResponseDTO(1L, "Drama"));

        MovieResponseDTO dto2 = new MovieResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Barbie");
        dto2.setSynopsis("Aventura em Barbielândia");
        dto2.setReleaseYear(2023);
        dto2.setDuration(115);
        dto2.setContentRating(ContentRating.AL);
        dto2.getGenres().add(new GenreResponseDTO(2L, "Aventura"));

        PagedResponse<MovieResponseDTO> pagedResponse = new PagedResponse<>(
            List.of(dto1, dto2),
            0, 2, 2L, 1, true
        );

        when(movieService.getMoviesByReleaseYear(year, pageable)).thenReturn(pagedResponse);

        // When
        ResponseEntity<PagedResponse<MovieResponseDTO>> response = movieController.getMoviesByReleaseYear(year, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());

        List<GenreResponseDTO> genresDto1 = new ArrayList<>(response.getBody().getContent().get(0).getGenres());
        assertEquals("Oppenheimer", response.getBody().getContent().get(0).getTitle());
        assertEquals("Drama", genresDto1.get(0).getName());

        List<GenreResponseDTO> genresDto2 = new ArrayList<>(response.getBody().getContent().get(1).getGenres());
        assertEquals("Barbie", response.getBody().getContent().get(1).getTitle());
        assertEquals("Aventura", genresDto2.get(0).getName());
}

    @Test
    void testGetMoviesByReleaseYear_EmptyResult() {
        // Given
        int year = 1900; // ano sem filmes
        Pageable pageable = PageRequest.of(0, 10);

        PagedResponse<MovieResponseDTO> emptyResponse = new PagedResponse<>(
            List.of(), 0, 0, 0L, 0, true
        );

        when(movieService.getMoviesByReleaseYear(year, pageable)).thenReturn(emptyResponse);

        // When
        ResponseEntity<PagedResponse<MovieResponseDTO>> response = movieController.getMoviesByReleaseYear(year, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
    }

@Test
void testCreateMovie_ReturnCreatedMovie() {
    // Given
    MovieRequestDTO requestDTO = new MovieRequestDTO();
    requestDTO.setTitle("Duna");
    requestDTO.setSynopsis("Ficção científica épica");
    requestDTO.setReleaseYear(2021);
    requestDTO.setDuration(155);
    requestDTO.setContentRating("A12");
    Set<Genre> genres = new java.util.HashSet<>();
    genres.add(new Genre("Ficção Científica"));
    genres.add(new Genre("Aventura"));
    requestDTO.setGenres(genres);

    MovieResponseDTO responseDTO = new MovieResponseDTO();
    responseDTO.setId(10L);
    responseDTO.setTitle("Duna");
    responseDTO.setSynopsis("Ficção científica épica");
    responseDTO.setReleaseYear(2021);
    responseDTO.setDuration(155);
    responseDTO.setContentRating(ContentRating.A12);
    responseDTO.getGenres().add(new GenreResponseDTO(1L, "Ficção Científica"));
    responseDTO.getGenres().add(new GenreResponseDTO(2L, "Aventura"));

    when(movieService.createMovie(requestDTO)).thenReturn(responseDTO);

    // When
    ResponseEntity<MovieResponseDTO> response = movieController.createMovie(requestDTO);

    // Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(10L, response.getBody().getId());
    assertEquals("Duna", response.getBody().getTitle());
}

@Test
void testCreateMovie_TitleAlreadyExists_ThrowsException() {
    // Given
    MovieRequestDTO requestDTO = new MovieRequestDTO();
    requestDTO.setTitle("Duna");
    requestDTO.setSynopsis("Ficção científica épica");
    requestDTO.setReleaseYear(2021);
    requestDTO.setDuration(155);
    requestDTO.setContentRating("A12");
    Set<Genre> genres = new java.util.HashSet<>();
    genres.add(new Genre("Ficção Científica"));
    genres.add(new Genre("Aventura"));

    when(movieService.createMovie(requestDTO))
        .thenThrow(new ValidationException("Título já existe"));

    // When / Then
    ValidationException exception = assertThrows(ValidationException.class, () -> {
        movieController.createMovie(requestDTO);
    });

    assertEquals("Título já existe", exception.getMessage());
}

    @Test
    void testUpdateMovie_ReturnUpdatedMovie() {
        // Given
        Long movieId = 5L;

        MovieRequestDTO requestDTO = new MovieRequestDTO();
        requestDTO.setTitle("Matrix Reloaded");
        requestDTO.setSynopsis("Sequência da Matrix");
        requestDTO.setReleaseYear(2003);
        requestDTO.setDuration(138);
        requestDTO.setContentRating("A14");
        Set<Genre> genres = new java.util.HashSet<>();
        genres.add(new Genre("Ação"));
        genres.add(new Genre("Ficção Científica"));

        MovieResponseDTO responseDTO = new MovieResponseDTO();
        responseDTO.setId(movieId);
        responseDTO.setTitle("Matrix Reloaded");
        responseDTO.setSynopsis("Sequência da Matrix");
        responseDTO.setReleaseYear(2003);
        responseDTO.setDuration(138);
        responseDTO.setContentRating(ContentRating.A14);
        responseDTO.getGenres().add(new GenreResponseDTO(1L, "Ação"));
        responseDTO.getGenres().add(new GenreResponseDTO(3L, "Ficção Científica"));

        when(movieService.updateMovie(movieId, requestDTO)).thenReturn(responseDTO);

        // When
        ResponseEntity<MovieResponseDTO> response = movieController.updateMovie(movieId, requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(movieId, response.getBody().getId());
        assertEquals("Matrix Reloaded", response.getBody().getTitle());
    }

    @Test
    void testUpdateMovie_MovieNotFound_ThrowsException() {
        // Given
        Long movieId = 999L;
        MovieRequestDTO requestDTO = new MovieRequestDTO();
        requestDTO.setTitle("Título qualquer");
        requestDTO.setSynopsis("Sinopse qualquer");
        requestDTO.setReleaseYear(2022);
        requestDTO.setDuration(120);
        requestDTO.setContentRating("A12");
        Set<Genre> genres = new java.util.HashSet<>();
        genres.add(new Genre("Aventura"));
        genres.add(new Genre("Comédia"));

        when(movieService.updateMovie(movieId, requestDTO))
            .thenThrow(new ResourceNotFoundException("Filme não encontrado com id " + movieId));

        // When 
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            movieController.updateMovie(movieId, requestDTO);
        });
        
        // Then
        assertEquals("Filme não encontrado com id " + movieId, exception.getMessage());
    }

    @Test
    void testPatchMovie_ReturnPatchedMovie() {
        // Given
        Long movieId = 10L;

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setTitle(Optional.of("Matrix Revolutions")); 

        MovieResponseDTO responseDTO = new MovieResponseDTO();
        responseDTO.setId(movieId);
        responseDTO.setTitle("Matrix Revolutions");
        responseDTO.setSynopsis("Fim da trilogia Matrix");
        responseDTO.setReleaseYear(2003);
        responseDTO.setDuration(129);
        responseDTO.setContentRating(ContentRating.A14);
        responseDTO.getGenres().add(new GenreResponseDTO(1L, "Ação"));
        responseDTO.getGenres().add(new GenreResponseDTO(2L, "Ficção Científica"));

        // When
        when(movieService.patchMovie(movieId, patchDTO)).thenReturn(responseDTO);
        ResponseEntity<MovieResponseDTO> response = movieController.patchMovie(movieId, patchDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Matrix Revolutions", response.getBody().getTitle());
        assertEquals(2003, response.getBody().getReleaseYear());
    }

    @Test
    void testPatchMovie_MovieNotFound_ThrowsException() {
        // Given
        Long movieId = 999L;

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setTitle(Optional.of("Matrix Revolutions")); 

        when(movieService.patchMovie(movieId, patchDTO))
            .thenThrow(new ResourceNotFoundException("Filme não encontrado com id " + movieId));

        // When 
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            movieController.patchMovie(movieId, patchDTO);
        });

        // Then
        assertEquals("Filme não encontrado com id " + movieId, exception.getMessage());
    }

    @Test
    void testDeleteMovie_ReturnNoContent() {
        // Given
        Long movieId = 1L;

        doNothing().when(movieService).deleteMovie(movieId);

        // When
        ResponseEntity<Void> response = movieController.deleteMovie(movieId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(movieService, times(1)).deleteMovie(movieId);
    }

    @Test
    void testDeleteMovie_ThrowsNotFoundException() {
        // Given
        Long movieId = 100L;

        doThrow(new ResourceNotFoundException("Filme não encontrado com ID: " + movieId))
            .when(movieService).deleteMovie(movieId);

        // When 
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> movieController.deleteMovie(movieId)
        );

        // Then
        assertEquals("Filme não encontrado com ID: 100", exception.getMessage());
        verify(movieService, times(1)).deleteMovie(movieId);
    }


    }
