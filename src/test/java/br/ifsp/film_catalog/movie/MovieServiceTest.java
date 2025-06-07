package br.ifsp.film_catalog.movie;

import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.MovieService;
import br.ifsp.film_catalog.repository.GenreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

    @Spy // Using Spy for ModelMapper to test its actual mapping logic for patch
    private ModelMapper modelMapper; // Real ModelMapper for testing patch logic

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie movie1;
    private MovieResponseDTO movieResponseDTO1;
    private MovieRequestDTO movieRequestDTO1;
    private Genre genreAction;

    @BeforeEach
    void setUp() {
        // Configure ModelMapper for PATCH to skip nulls
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT) // Or LOOSE depending on needs
            .setPropertyCondition(context -> context.getSource() != null);


        genreAction = new Genre("Action");
        genreAction.setId(1L);

        movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Inception");
        movie1.setSynopsis("A mind-bending thriller");
        movie1.setReleaseYear(2010);
        movie1.setDuration(148);
        movie1.setContentRating(ContentRating.A12);
        movie1.addGenre(genreAction);


        movieResponseDTO1 = new MovieResponseDTO(1L, "Inception", "A mind-bending thriller", 2010, 148, ContentRating.A12,
                Set.of(new br.ifsp.film_catalog.dto.GenreResponseDTO(1L, "Action")));

        movieRequestDTO1 = new MovieRequestDTO("Inception", "A mind-bending thriller", 2010, 148, "A12", Set.of(genreAction));
    }

    @Test
    void getAllMovies_shouldReturnPagedResponseOfMovies() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Movie> movies = Collections.singletonList(movie1);
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        PagedResponse<MovieResponseDTO> expectedPagedResponse = new PagedResponse<>(
                Collections.singletonList(movieResponseDTO1), 0, 10, 1, 1, true
        );

        when(movieRepository.findAll(pageable)).thenReturn(moviePage);
        when(pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class)).thenReturn(expectedPagedResponse);

        PagedResponse<MovieResponseDTO> actualResponse = movieService.getAllMovies(pageable);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getContent()).hasSize(1);
        assertThat(actualResponse.getContent().get(0).getTitle()).isEqualTo("Inception");
        verify(movieRepository).findAll(pageable);
    }

    @Test
    void getMovieById_whenMovieExists_shouldReturnMovieResponseDTO() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie1));
        when(modelMapper.map(movie1, MovieResponseDTO.class)).thenReturn(movieResponseDTO1);

        MovieResponseDTO found = movieService.getMovieById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Inception");
        verify(movieRepository).findById(1L);
    }

    @Test
    void getMovieById_whenMovieNotFound_shouldThrowResourceNotFoundException() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(1L));
    }

    @Test
    void getMoviesByTitle_shouldReturnPagedMovies() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1), pageable, 1);
        PagedResponse<MovieResponseDTO> expectedResponse = new PagedResponse<>(List.of(movieResponseDTO1), 0, 5, 1, 1, true);

        when(movieRepository.findByTitleContainingIgnoreCase("Inception", pageable)).thenReturn(moviePage);
        when(pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class)).thenReturn(expectedResponse);

        PagedResponse<MovieResponseDTO> result = movieService.getMoviesByTitle("Inception", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Inception");
        verify(movieRepository).findByTitleContainingIgnoreCase("Inception", pageable);
    }
    /* 
    @Test
    void getMoviesByGenre_whenGenreExists_shouldReturnPagedMovies() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1), pageable, 1);
        PagedResponse<MovieResponseDTO> expectedResponse = new PagedResponse<>(List.of(movieResponseDTO1), 0, 5, 1, 1, true);

        when(genreRepository.findByNameIgnoreCase("Action")).thenReturn(Optional.of(genreAction));
        when(movieRepository.findByGenresContaining(genreAction.getId(), pageable)).thenReturn(moviePage); // Assuming findByGenresContaining takes Genre object
        when(pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class)).thenReturn(expectedResponse);

        PagedResponse<MovieResponseDTO> result = movieService.getMoviesByGenre("Action", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGenres().iterator().next().getName()).isEqualTo("Action");
        verify(genreRepository).findByNameIgnoreCase("Action");
        verify(movieRepository).findByGenresContaining(genreAction.getId(), pageable);
    }*/

    /*
    @Test
    void getMoviesByGenre_whenGenreNotFound_shouldThrowResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 5);
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMoviesByGenre("Fantasy", pageable));
        verify(movieRepository, never()).findByGenresContaining(any(Long.class), any(Pageable.class));
    }*/

@Test
    void createMovie_shouldReturnResponseDTO_whenValidRequest() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Action");

        MovieRequestDTO requestDTO = new MovieRequestDTO();
        requestDTO.setTitle("Inception");
        requestDTO.setGenres(Set.of(genre));

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.empty());
        when(genreRepository.findByNameIgnoreCase("Action")).thenReturn(Optional.of(genre));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovieResponseDTO responseDTO = movieService.createMovie(requestDTO);

        assertNotNull(responseDTO);
        assertEquals("Inception", responseDTO.getTitle());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void createMovie_shouldThrowException_whenTitleAlreadyExists() {
        MovieRequestDTO requestDTO = new MovieRequestDTO();
        requestDTO.setTitle("Inception");

        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(new Movie()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> movieService.createMovie(requestDTO));

        assertEquals("Movie with title 'Inception' already exists.", exception.getMessage());
    }

    @Test
    void patchMovie_shouldReturnUpdatedMovie_whenValidId() {
        Long movieId = 1L;

        Movie existingMovie = new Movie();
        existingMovie.setId(movieId);
        existingMovie.setTitle("Old Title");

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setTitle("New Title");

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieResponseDTO result = movieService.patchMovie(movieId, patchDTO);

        assertEquals("New Title", result.getTitle());
        verify(movieRepository).save(existingMovie);
    }

    @Test
    void patchMovie_shouldThrowException_whenMovieNotFound() {
        Long movieId = 99L;
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setTitle("New Title");

        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> movieService.patchMovie(movieId, patchDTO));

        assertEquals("Movie id not found: 99", exception.getMessage());
    }


    @Test
    void createMovie_whenTitleIsNotUnique_shouldThrowIllegalArgumentException() {
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie1));
        assertThrows(IllegalArgumentException.class, () -> movieService.createMovie(movieRequestDTO1));
        verify(movieRepository, never()).save(any(Movie.class));
    }


    @Test
    void deleteMovie_whenMovieExists_shouldDeleteMovie() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        doNothing().when(movieRepository).deleteById(1L);

        movieService.deleteMovie(1L);

        verify(movieRepository).deleteById(1L);
    }

    @Test
    void deleteMovie_whenMovieNotFound_shouldThrowResourceNotFoundException() {
        when(movieRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> movieService.deleteMovie(1L));
        verify(movieRepository, never()).deleteById(anyLong());
    }
}
