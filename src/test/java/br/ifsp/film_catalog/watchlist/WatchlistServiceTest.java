package br.ifsp.film_catalog.watchlist;

import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.WatchlistRequestDTO;
import br.ifsp.film_catalog.dto.WatchlistResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.Watchlist;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.WatchlistRepository;
import br.ifsp.film_catalog.service.WatchlistService;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Spy // Usar Spy para ModelMapper para testar sua lógica real de mapeamento se necessário,
         // ou Mock para controle total. Para testes de serviço, Spy pode ser útil
         // para garantir que o mapeamento real esteja ocorrendo como esperado.
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private WatchlistService watchlistService;

    private User user1;
    private Movie movie1;
    private Movie movie2;
    private Watchlist watchlist1;
    private WatchlistRequestDTO watchlistRequestDTO1;
    private WatchlistResponseDTO watchlistResponseDTO1;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");

        movie1 = new Movie();
        movie1.setId(10L);
        movie1.setTitle("Inception");

        movie2 = new Movie();
        movie2.setId(11L);
        movie2.setTitle("Interstellar");

        watchlist1 = new Watchlist();
        watchlist1.setId(100L);
        watchlist1.setName("My Sci-Fi");
        watchlist1.setDescription("Best sci-fi movies");
        watchlist1.setUser(user1);
        watchlist1.setMovies(new HashSet<>(Set.of(movie1))); // Watchlist começa com movie1

        watchlistRequestDTO1 = new WatchlistRequestDTO("My Sci-Fi", "Best sci-fi movies");
        
        // Configurar watchlistResponseDTO1 manualmente para refletir o estado esperado após conversão
        watchlistResponseDTO1 = new WatchlistResponseDTO();
        watchlistResponseDTO1.setId(100L);
        watchlistResponseDTO1.setName("My Sci-Fi");
        watchlistResponseDTO1.setDescription("Best sci-fi movies");
        watchlistResponseDTO1.setUserId(user1.getId());
        MovieResponseDTO movieResponseDTO1 = new MovieResponseDTO(); // Supondo que MovieResponseDTO tenha construtor e setters
        movieResponseDTO1.setId(movie1.getId());
        movieResponseDTO1.setTitle(movie1.getTitle());
        watchlistResponseDTO1.setMovies(Set.of(movieResponseDTO1));


        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createWatchlist_whenUserExists_shouldCreateAndReturnWatchlist() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Watchlist transientWatchlist = new Watchlist();
        transientWatchlist.setName(watchlistRequestDTO1.getName());
        transientWatchlist.setDescription(watchlistRequestDTO1.getDescription());

        Watchlist savedWatchlist = new Watchlist();
        savedWatchlist.setId(200L);
        savedWatchlist.setName(watchlistRequestDTO1.getName());
        savedWatchlist.setDescription(watchlistRequestDTO1.getDescription());
        savedWatchlist.setUser(user1);
        savedWatchlist.setMovies(new HashSet<>());

        doAnswer(invocation -> {
            WatchlistRequestDTO source = invocation.getArgument(0);
            Watchlist destination = invocation.getArgument(1);
            destination.setName(source.getName());
            destination.setDescription(source.getDescription());
            return null; 
        }).when(modelMapper).map(eq(watchlistRequestDTO1), any(Watchlist.class));

        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            Watchlist wlToSave = invocation.getArgument(0);
            savedWatchlist.setName(wlToSave.getName());
            savedWatchlist.setDescription(wlToSave.getDescription());
            savedWatchlist.setUser(wlToSave.getUser());
            return savedWatchlist;
        });

        // Just return a dummy DTO, not using convertToResponseDTOPrivate anymore
        WatchlistResponseDTO expectedResponse = new WatchlistResponseDTO();
        expectedResponse.setId(savedWatchlist.getId());
        expectedResponse.setName(savedWatchlist.getName());
        expectedResponse.setDescription(savedWatchlist.getDescription());
        expectedResponse.setUserId(user1.getId());
        expectedResponse.setMovies(new HashSet<>());

        when(modelMapper.map(savedWatchlist, WatchlistResponseDTO.class)).thenReturn(expectedResponse);

        WatchlistResponseDTO result = watchlistService.createWatchlist(user1.getId(), watchlistRequestDTO1);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(watchlistRequestDTO1.getName());
        assertThat(result.getUserId()).isEqualTo(user1.getId());
        assertThat(result.getMovies()).isEmpty();
        verify(userRepository).findById(user1.getId());
        verify(watchlistRepository).save(any(Watchlist.class));
        verify(modelMapper).map(eq(watchlistRequestDTO1), any(Watchlist.class));
        verify(modelMapper).map(eq(savedWatchlist), eq(WatchlistResponseDTO.class));
    }

    @Test
    void createWatchlist_whenUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.createWatchlist(99L, watchlistRequestDTO1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void getWatchlistsByUser_whenUserExists_shouldReturnPagedWatchlists() {
        Page<Watchlist> watchlistPage = new PageImpl<>(List.of(watchlist1), pageable, 1);
        PagedResponse<WatchlistResponseDTO> expectedPagedResponse = new PagedResponse<>(
                List.of(watchlistResponseDTO1), 0, 10, 1, 1, true);

        when(userRepository.existsById(user1.getId())).thenReturn(true);
        when(watchlistRepository.findAllByUser_Id(user1.getId(), pageable)).thenReturn(watchlistPage);
        when(pagedResponseMapper.toPagedResponse(eq(watchlistPage), eq(WatchlistResponseDTO.class)))
             .thenReturn(expectedPagedResponse);


        PagedResponse<WatchlistResponseDTO> result = watchlistService.getWatchlistsByUser(user1.getId(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(watchlist1.getName());
        verify(userRepository).existsById(user1.getId());
        verify(watchlistRepository).findAllByUser_Id(user1.getId(), pageable);
    }

    @Test
    void getWatchlistsByUser_whenUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> watchlistService.getWatchlistsByUser(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
        verify(watchlistRepository, never()).findAllByUser_Id(anyLong(), any(Pageable.class));
    }

    @Test
    void getWatchlistByIdAndUser_whenExists_shouldReturnWatchlist() {
        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        when(modelMapper.map(watchlist1, WatchlistResponseDTO.class)).thenReturn(watchlistResponseDTO1);


        WatchlistResponseDTO result = watchlistService.getWatchlistByIdAndUser(user1.getId(), watchlist1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(watchlist1.getId());
        assertThat(result.getName()).isEqualTo(watchlist1.getName());
        assertThat(result.getUserId()).isEqualTo(user1.getId());
        verify(watchlistRepository).findByIdAndUserId(watchlist1.getId(), user1.getId());
    }

    @Test
    void getWatchlistByIdAndUser_whenNotFound_shouldThrowResourceNotFoundException() {
        when(watchlistRepository.findByIdAndUserId(999L, user1.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.getWatchlistByIdAndUser(user1.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Watchlist not found with id: 999 for user: " + user1.getId());
    }

    @Test
    void updateWatchlist_whenExists_shouldUpdateAndReturnWatchlist() {
        WatchlistRequestDTO updateRequest = new WatchlistRequestDTO("Updated Name", "Updated Desc");
        Watchlist updatedWatchlistEntity = new Watchlist();
        updatedWatchlistEntity.setId(watchlist1.getId());
        updatedWatchlistEntity.setName(updateRequest.getName());
        updatedWatchlistEntity.setDescription(updateRequest.getDescription());
        updatedWatchlistEntity.setUser(user1);
        updatedWatchlistEntity.setMovies(watchlist1.getMovies());

        WatchlistResponseDTO expectedResponse = new WatchlistResponseDTO();
        expectedResponse.setId(updatedWatchlistEntity.getId());
        expectedResponse.setName(updatedWatchlistEntity.getName());
        expectedResponse.setDescription(updatedWatchlistEntity.getDescription());
        expectedResponse.setUserId(user1.getId());
        // Not mapping movies for simplicity

        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(updatedWatchlistEntity);
        when(modelMapper.map(updatedWatchlistEntity, WatchlistResponseDTO.class)).thenReturn(expectedResponse);

        WatchlistResponseDTO result = watchlistService.updateWatchlist(user1.getId(), watchlist1.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Desc");
        verify(watchlistRepository).save(argThat(wl -> 
            wl.getName().equals("Updated Name") && wl.getDescription().equals("Updated Desc")
        ));
    }

    @Test
    void updateWatchlist_whenNotFound_shouldThrowResourceNotFoundException() {
        WatchlistRequestDTO updateRequest = new WatchlistRequestDTO("Updated Name", "Updated Desc");
        when(watchlistRepository.findByIdAndUserId(999L, user1.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.updateWatchlist(user1.getId(), 999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void deleteWatchlist_whenExists_shouldDeleteWatchlist() {
        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        doNothing().when(watchlistRepository).delete(watchlist1);

        watchlistService.deleteWatchlist(user1.getId(), watchlist1.getId());

        verify(watchlistRepository).delete(watchlist1);
    }

    @Test
    void deleteWatchlist_whenNotFound_shouldThrowResourceNotFoundException() {
        when(watchlistRepository.findByIdAndUserId(999L, user1.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.deleteWatchlist(user1.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(watchlistRepository, never()).delete(any(Watchlist.class));
    }

    @Test
    void addMovieToWatchlist_whenMovieNotPresent_shouldAddAndReturnWatchlist() {
        Watchlist watchlistCopy = new Watchlist();
        watchlistCopy.setId(watchlist1.getId());
        watchlistCopy.setName(watchlist1.getName());
        watchlistCopy.setUser(user1);
        watchlistCopy.setMovies(new HashSet<>(Set.of(movie1)));

        Watchlist watchlistAfterAdd = new Watchlist();
        watchlistAfterAdd.setId(watchlist1.getId());
        watchlistAfterAdd.setName(watchlist1.getName());
        watchlistAfterAdd.setUser(user1);
        watchlistAfterAdd.setMovies(new HashSet<>(Set.of(movie1, movie2)));

        WatchlistResponseDTO expectedResponse = new WatchlistResponseDTO();
        expectedResponse.setId(watchlistAfterAdd.getId());
        expectedResponse.setName(watchlistAfterAdd.getName());
        expectedResponse.setUserId(user1.getId());
        // Not mapping movies for simplicity

        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlistCopy));
        when(movieRepository.findById(movie2.getId())).thenReturn(Optional.of(movie2));
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(watchlistAfterAdd);
        when(modelMapper.map(watchlistAfterAdd, WatchlistResponseDTO.class)).thenReturn(expectedResponse);

        WatchlistResponseDTO result = watchlistService.addMovieToWatchlist(user1.getId(), watchlist1.getId(), movie2.getId());

        assertThat(result).isNotNull();
        verify(watchlistRepository).save(argThat(wl -> wl.getMovies().contains(movie2)));
    }

    @Test
    void addMovieToWatchlist_whenMovieAlreadyPresent_shouldNotAddAgainAndReturnWatchlist() {
        WatchlistResponseDTO expectedResponse = new WatchlistResponseDTO();
        expectedResponse.setId(watchlist1.getId());
        expectedResponse.setName(watchlist1.getName());
        expectedResponse.setUserId(user1.getId());
        // Not mapping movies for simplicity

        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        when(movieRepository.findById(movie1.getId())).thenReturn(Optional.of(movie1));
        when(modelMapper.map(watchlist1, WatchlistResponseDTO.class)).thenReturn(expectedResponse);

        WatchlistResponseDTO result = watchlistService.addMovieToWatchlist(user1.getId(), watchlist1.getId(), movie1.getId());

        assertThat(result).isNotNull();
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void addMovieToWatchlist_whenWatchlistNotFound_shouldThrowResourceNotFoundException() {
        when(watchlistRepository.findByIdAndUserId(999L, user1.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.addMovieToWatchlist(user1.getId(), 999L, movie1.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Watchlist not found");
        verify(movieRepository, never()).findById(anyLong());
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void addMovieToWatchlist_whenMovieNotFound_shouldThrowResourceNotFoundException() {
        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        when(movieRepository.findById(990L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.addMovieToWatchlist(user1.getId(), watchlist1.getId(), 990L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found");
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void removeMovieFromWatchlist_whenMoviePresent_shouldRemoveAndReturnWatchlist() {
        Watchlist watchlistBeforeRemove = new Watchlist();
        watchlistBeforeRemove.setId(watchlist1.getId());
        watchlistBeforeRemove.setName(watchlist1.getName());
        watchlistBeforeRemove.setUser(user1);
        watchlistBeforeRemove.setMovies(new HashSet<>(Set.of(movie1)));

        Watchlist watchlistAfterRemove = new Watchlist();
        watchlistAfterRemove.setId(watchlist1.getId());
        watchlistAfterRemove.setName(watchlist1.getName());
        watchlistAfterRemove.setUser(user1);
        watchlistAfterRemove.setMovies(new HashSet<>());

        WatchlistResponseDTO expectedResponse = new WatchlistResponseDTO();
        expectedResponse.setId(watchlistAfterRemove.getId());
        expectedResponse.setName(watchlistAfterRemove.getName());
        expectedResponse.setUserId(user1.getId());
        // Not mapping movies for simplicity

        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlistBeforeRemove));
        when(movieRepository.findById(movie1.getId())).thenReturn(Optional.of(movie1));
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(watchlistAfterRemove);
        when(modelMapper.map(watchlistAfterRemove, WatchlistResponseDTO.class)).thenReturn(expectedResponse);

        WatchlistResponseDTO result = watchlistService.removeMovieFromWatchlist(user1.getId(), watchlist1.getId(), movie1.getId());

        assertThat(result).isNotNull();
        verify(watchlistRepository).save(argThat(wl -> wl.getMovies().isEmpty()));
    }

    @Test
    void removeMovieFromWatchlist_whenMovieNotPresent_shouldDoNothingAndReturnWatchlist() {
        WatchlistResponseDTO expectedResponse = new WatchlistResponseDTO();
        expectedResponse.setId(watchlist1.getId());
        expectedResponse.setName(watchlist1.getName());
        expectedResponse.setUserId(user1.getId());
        // Not mapping movies for simplicity

        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        when(movieRepository.findById(movie2.getId())).thenReturn(Optional.of(movie2));
        when(watchlistRepository.save(watchlist1)).thenReturn(watchlist1);
        when(modelMapper.map(watchlist1, WatchlistResponseDTO.class)).thenReturn(expectedResponse);

        WatchlistResponseDTO result = watchlistService.removeMovieFromWatchlist(user1.getId(), watchlist1.getId(), movie2.getId());

        assertThat(result).isNotNull();
        verify(watchlistRepository).save(watchlist1);
    }

    @Test
    void removeMovieFromWatchlist_whenWatchlistNotFound_shouldThrowResourceNotFoundException() {
        when(watchlistRepository.findByIdAndUserId(999L, user1.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.removeMovieFromWatchlist(user1.getId(), 999L, movie1.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(movieRepository, never()).findById(anyLong());
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void removeMovieFromWatchlist_whenMovieNotFound_shouldThrowResourceNotFoundException() {
        when(watchlistRepository.findByIdAndUserId(watchlist1.getId(), user1.getId())).thenReturn(Optional.of(watchlist1));
        when(movieRepository.findById(990L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.removeMovieFromWatchlist(user1.getId(), watchlist1.getId(), 990L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }
}
