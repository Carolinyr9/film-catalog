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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    }


    @Test
    void getMoviesByGenre_whenGenreNotFound_shouldThrowResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 5);
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMoviesByGenre("Fantasy", pageable));
        verify(movieRepository, never()).findByGenresContaining(any(Long.class), any(Pageable.class));
    }

/* 
    @Test
    void createMovie_whenTitleIsUnique_shouldCreateAndReturnMovie() {
        Movie movieToSave = new Movie(); // Entity before save (no ID)
        // Simulate mapping from DTO to this new entity
        when(modelMapper.map(movieRequestDTO1, Movie.class)).thenReturn(movieToSave);
        // Simulate what happens after mapping and before saving
        movieToSave.setTitle(movieRequestDTO1.getTitle());
        // ... other fields from movieRequestDTO1
        
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreAction)); // For genreIds
        
        // Simulate saving the entity (it gets an ID)
        Movie savedMovie = new Movie(); // Entity after save (with ID)
        savedMovie.setId(1L);
        savedMovie.setTitle(movieRequestDTO1.getTitle());
        // ... other fields ...
        savedMovie.addGenre(genreAction); // Simulate genre linking in service or entity

        when(movieRepository.findByTitle(movieRequestDTO1.getTitle())).thenReturn(Optional.empty());
        when(movieRepository.save(movieToSave)).thenReturn(savedMovie); // Return the entity with ID
        when(modelMapper.map(savedMovie, MovieResponseDTO.class)).thenReturn(movieResponseDTO1);


        MovieResponseDTO created = movieService.createMovie(movieRequestDTO1);

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Inception");
        assertThat(created.getGenres()).hasSize(1);
        verify(movieRepository).save(movieToSave);
    }
        */

    @Test
    void createMovie_whenTitleIsNotUnique_shouldThrowIllegalArgumentException() {
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie1));
        assertThrows(IllegalArgumentException.class, () -> movieService.createMovie(movieRequestDTO1));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    /**
     * Test for updating a movie when the title is unique.
     * This test ensures that the service correctly updates the movie and returns the updated DTO.
     
    @Test
    void patchMovie_whenMovieExists_shouldUpdateAndReturnMovie() {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setTitle(Optional.of("Inception Remastered"));
        patchDTO.setSynopsis(Optional.empty()); // Not changing synopsis
        patchDTO.setReleaseYear(Optional.empty());
        patchDTO.setDuration(Optional.empty());
        patchDTO.setContentRating(Optional.empty());
        patchDTO.setGenreIds(Optional.of(Set.of(genreAction.getId())));

        Movie movieFromDb = new Movie(); // Simulate movie fetched from DB
        movieFromDb.setId(1L);
        movieFromDb.setTitle("Inception");
        movieFromDb.setSynopsis("Old Synopsis");
        movieFromDb.setContentRating(ContentRating.A10);

        MovieResponseDTO expectedResponseDTO = new MovieResponseDTO();
        expectedResponseDTO.setId(1L);
        expectedResponseDTO.setTitle("Inception Remastered");
        expectedResponseDTO.setSynopsis("Old Synopsis");
        expectedResponseDTO.setContentRating(ContentRating.A10);
        expectedResponseDTO.setGenres(Set.of(new br.ifsp.film_catalog.dto.GenreResponseDTO(genreAction.getId(), genreAction.getName())));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieFromDb));
        when(genreRepository.findById(genreAction.getId())).thenReturn(Optional.of(genreAction));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.getGenres().clear();
            m.addGenre(genreAction);
            return m;
        });
        doReturn(expectedResponseDTO).when(modelMapper).map(any(Movie.class), eq(MovieResponseDTO.class));

        // Ensure patch logic is consistent with service and test setup
        MovieResponseDTO patched = movieService.patchMovie(1L, patchDTO);

        assertThat(patched).isNotNull();
        assertThat(patched.getTitle()).isEqualTo("Inception Remastered");
        assertThat(patched.getSynopsis()).isEqualTo("Old Synopsis"); // Should not have changed
        assertThat(patched.getGenres().iterator().next().getName()).isEqualTo("Action");

        verify(movieRepository).findById(1L);
        verify(movieRepository).save(any(Movie.class));
    }
*/

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
