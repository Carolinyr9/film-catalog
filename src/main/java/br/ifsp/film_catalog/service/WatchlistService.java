package br.ifsp.film_catalog.service;

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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public WatchlistService(WatchlistRepository watchlistRepository,
                              UserRepository userRepository,
                              MovieRepository movieRepository,
                              ModelMapper modelMapper,
                              PagedResponseMapper pagedResponseMapper) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public WatchlistResponseDTO createWatchlist(Long userId, WatchlistRequestDTO watchlistRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Watchlist watchlist = new Watchlist();
        modelMapper.map(watchlistRequestDTO, watchlist);

        watchlist.setUser(user);

        Watchlist savedWatchlist = watchlistRepository.save(watchlist);
        return modelMapper.map(savedWatchlist, WatchlistResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WatchlistResponseDTO> getWatchlistsByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        Page<Watchlist> watchlistPage = watchlistRepository.findAllByUser_Id(userId, pageable);
        return pagedResponseMapper.toPagedResponse(watchlistPage, WatchlistResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public WatchlistResponseDTO getWatchlistByIdAndUser(Long userId, Long watchlistId) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with id: " + watchlistId + " for user: " + userId));
        return modelMapper.map(watchlist, WatchlistResponseDTO.class);
    }

    @Transactional
    public WatchlistResponseDTO updateWatchlist(Long userId, Long watchlistId, WatchlistRequestDTO watchlistRequestDTO) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with id: " + watchlistId + " for user: " + userId));

        watchlist.setName(watchlistRequestDTO.getName());
        watchlist.setDescription(watchlistRequestDTO.getDescription());

        Watchlist updatedWatchlist = watchlistRepository.save(watchlist);
        return modelMapper.map(updatedWatchlist, WatchlistResponseDTO.class);
    }

    @Transactional
    public void deleteWatchlist(Long userId, Long watchlistId) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with id: " + watchlistId + " for user: " + userId));
        watchlistRepository.delete(watchlist);
    }

    @Transactional
    public WatchlistResponseDTO addMovieToWatchlist(Long userId, Long watchlistId, Long movieId) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with id: " + watchlistId + " for user: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        // Verifica se o filme já está na watchlist para evitar duplicatas na tabela de junção
        if (watchlist.getMovies().stream().noneMatch(m -> m.getId().equals(movieId))) {
            watchlist.addMovie(movie);
            watchlistRepository.save(watchlist);
        }
        return modelMapper.map(watchlist, WatchlistResponseDTO.class);
    }

    @Transactional
    public WatchlistResponseDTO removeMovieFromWatchlist(Long userId, Long watchlistId, Long movieId) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with id: " + watchlistId + " for user: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        watchlist.removeMovie(movie);
        watchlistRepository.save(watchlist);
        return modelMapper.map(watchlist, WatchlistResponseDTO.class);
    }
}
