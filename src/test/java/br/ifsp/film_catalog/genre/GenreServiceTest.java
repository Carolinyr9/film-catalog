package br.ifsp.film_catalog.genre;

import br.ifsp.film_catalog.dto.GenreRequestDTO;
import br.ifsp.film_catalog.dto.GenreResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.repository.GenreRepository;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.GenreService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieRepository movieRepository;

    @Spy // Use Spy for ModelMapper if you want to test its actual mapping logic
         // or @Mock if you want to fully control its behavior.
         // For unit testing the service, usually @Mock is preferred for mappers.
         // Let's use @Mock here for simplicity of service test.
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private GenreService genreService;

    private Genre genre1;
    private GenreResponseDTO genreResponseDTO1;
    private GenreRequestDTO genreRequestDTO1;

    @BeforeEach
    void setUp() {
        genre1 = new Genre("Action");
        genre1.setId(1L); 

        genreResponseDTO1 = new GenreResponseDTO(1L, "Action");
        genreRequestDTO1 = new GenreRequestDTO("Action");
    }

    @Test
    void getAllGenres_shouldReturnPagedResponseOfGenres() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Genre> genres = Collections.singletonList(genre1);
        Page<Genre> genrePage = new PageImpl<>(genres, pageable, genres.size());
        PagedResponse<GenreResponseDTO> expectedPagedResponse = new PagedResponse<>(
                Collections.singletonList(genreResponseDTO1), 0, 10, 1, 1, true
        );

        when(genreRepository.findAll(pageable)).thenReturn(genrePage);
        when(pagedResponseMapper.toPagedResponse(genrePage, GenreResponseDTO.class)).thenReturn(expectedPagedResponse);

        PagedResponse<GenreResponseDTO> actualResponse = genreService.getAllGenres(pageable);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getContent()).hasSize(1);
        assertThat(actualResponse.getContent().get(0).getName()).isEqualTo("Action");
        verify(genreRepository).findAll(pageable);
        verify(pagedResponseMapper).toPagedResponse(genrePage, GenreResponseDTO.class);
    }

    @Test
    void getGenreById_whenGenreExists_shouldReturnGenreResponseDTO() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));
        when(modelMapper.map(genre1, GenreResponseDTO.class)).thenReturn(genreResponseDTO1);

        GenreResponseDTO found = genreService.getGenreById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Action");
        verify(genreRepository).findById(1L);
        verify(modelMapper).map(genre1, GenreResponseDTO.class);
    }

    @Test
    void getGenreById_whenGenreNotFound_shouldThrowResourceNotFoundException() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.getGenreById(1L));
        verify(genreRepository).findById(1L);
    }

    @Test
    void createGenre_whenNameIsUnique_shouldCreateAndReturnGenre() {
        when(genreRepository.findByNameIgnoreCase("Action")).thenReturn(Optional.empty());
        when(modelMapper.map(genreRequestDTO1, Genre.class)).thenReturn(genre1); // map DTO to entity
        when(genreRepository.save(any(Genre.class))).thenReturn(genre1); // save entity
        when(modelMapper.map(genre1, GenreResponseDTO.class)).thenReturn(genreResponseDTO1); // map entity to DTO

        GenreResponseDTO created = genreService.createGenre(genreRequestDTO1);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Action");
        verify(genreRepository).findByNameIgnoreCase("Action");
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void createGenre_whenNameIsNotUnique_shouldThrowIllegalArgumentException() {
        when(genreRepository.findByNameIgnoreCase("Action")).thenReturn(Optional.of(genre1));
        when(modelMapper.map(genreRequestDTO1, Genre.class)).thenReturn(new Genre("Action"));


        assertThrows(IllegalArgumentException.class, () -> genreService.createGenre(genreRequestDTO1));
        verify(genreRepository).findByNameIgnoreCase("Action");
        verify(genreRepository, never()).save(any(Genre.class));
    }

    @Test
    void updateGenre_whenGenreExistsAndNameIsUnique_shouldUpdateAndReturnGenre() {
        GenreRequestDTO updateRequest = new GenreRequestDTO("Updated Action");
        Genre updatedGenreEntity = new Genre("Updated Action");
        updatedGenreEntity.setId(1L);
        GenreResponseDTO updatedResponseDTO = new GenreResponseDTO(1L, "Updated Action");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));
        when(genreRepository.findByNameIgnoreCase("Updated Action")).thenReturn(Optional.empty());
        // modelMapper.map(updateRequest, genre1) will be called to update fields on genre1
        doNothing().when(modelMapper).map(updateRequest, genre1);
        when(genreRepository.save(genre1)).thenReturn(updatedGenreEntity); // save the modified genre1
        when(modelMapper.map(updatedGenreEntity, GenreResponseDTO.class)).thenReturn(updatedResponseDTO);

        GenreResponseDTO updated = genreService.updateGenre(1L, updateRequest);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Action");
        verify(modelMapper).map(updateRequest, genre1); // Verify mapping to existing entity
        verify(genreRepository).save(genre1);
    }
    
    @Test
    void updateGenre_whenNewNameBelongsToSameGenre_shouldUpdate() {
        GenreRequestDTO updateRequest = new GenreRequestDTO("Action"); // Same name
        Genre updatedGenreEntity = new Genre("Action");
        updatedGenreEntity.setId(1L);
        GenreResponseDTO updatedResponseDTO = new GenreResponseDTO(1L, "Action");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));
        when(genreRepository.findByNameIgnoreCase("Action")).thenReturn(Optional.of(genre1)); // Name exists but it's the same entity
        // Add this stub for modelMapper.map(updateRequest, genre1)
        doNothing().when(modelMapper).map(updateRequest, genre1);
        when(genreRepository.save(genre1)).thenReturn(updatedGenreEntity);
        when(modelMapper.map(updatedGenreEntity, GenreResponseDTO.class)).thenReturn(updatedResponseDTO);
        
        GenreResponseDTO updated = genreService.updateGenre(1L, updateRequest);
        
        assertThat(updated.getName()).isEqualTo("Action");
        verify(modelMapper).map(updateRequest, genre1);
        verify(genreRepository).save(genre1);
    }


    @Test
    void updateGenre_whenGenreNotFound_shouldThrowResourceNotFoundException() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> genreService.updateGenre(1L, genreRequestDTO1));
    }

    @Test
    void updateGenre_whenNameConflictsWithAnotherGenre_shouldThrowIllegalArgumentException() {
        Genre existingOtherGenre = new Genre("Existing Action");
        existingOtherGenre.setId(2L); // Different ID
        GenreRequestDTO updateRequest = new GenreRequestDTO("Existing Action");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));
        when(genreRepository.findByNameIgnoreCase("Existing Action")).thenReturn(Optional.of(existingOtherGenre));

        assertThrows(IllegalArgumentException.class, () -> genreService.updateGenre(1L, updateRequest));
        verify(genreRepository, never()).save(any(Genre.class));
    }

    @Test
    void deleteGenre_whenGenreExistsAndNotInUse_shouldDeleteGenre() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));
        when(movieRepository.existsByGenresId(1L)).thenReturn(false); // Genre not in use

        genreService.deleteGenre(1L);

        verify(genreRepository).delete(genre1);
    }

    @Test
    void deleteGenre_whenGenreNotFound_shouldThrowResourceNotFoundException() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> genreService.deleteGenre(1L));
        verify(movieRepository, never()).existsByGenresId(anyLong());
        verify(genreRepository, never()).delete(any(Genre.class));
    }

    @Test
    void deleteGenre_whenGenreIsInUse_shouldThrowIllegalStateException() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));
        when(movieRepository.existsByGenresId(1L)).thenReturn(true); // Genre is in use

        assertThrows(IllegalStateException.class, () -> genreService.deleteGenre(1L));
        verify(genreRepository, never()).delete(any(Genre.class));
    }
}
