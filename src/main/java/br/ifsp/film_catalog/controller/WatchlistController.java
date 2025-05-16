package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.WatchlistDTO;
import br.ifsp.film_catalog.exception.InvalidMovieStateException;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Watchlist;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.WatchlistRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Tag(name = "Watchlist", description = "API para catálogo de filmes")
@Validated
@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistRepository watchlistRepository;
    private final ModelMapper watchMapper;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Autowired
    public WatchlistController(ModelMapper watchMapper, WatchlistRepository watchlistRepository,
                               UserRepository userRepository, MovieRepository movieRepository) {
        this.watchMapper = watchMapper;
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Operation(summary = "Listar todas as watchlists de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlists encontradas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<Page<WatchlistDTO>> getAllWatchlistsByUser(@PathVariable Long userId, Pageable pageable) {
        var watchlists = watchlistRepository.findAllByUser_Id(userId, pageable)
                .map(watchlist -> watchMapper.map(watchlist, WatchlistDTO.class));
        return ResponseEntity.ok(watchlists);
    }

    @Operation(summary = "Cria uma nova watchlist para o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlist criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content)
    })
    @PostMapping("/register/{userId}")
    public ResponseEntity<?> createWatchlistByUser(@PathVariable Long userId, @RequestBody @Validated WatchlistDTO dto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Watchlist watchlist = watchMapper.map(dto, Watchlist.class);
        watchlist.setUser(user);
        watchlistRepository.save(watchlist);
        return ResponseEntity.ok("Watchlist criada com sucesso");
    }

    @Operation(summary = "Atualiza uma watchlist do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlist atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Watchlist não encontrada", content = @Content)
    })
    @PutMapping("/{userId}/{watchlistId}")
    public ResponseEntity<Void> updateWatchlistByUser(@PathVariable Long userId,
                                                      @PathVariable Long watchlistId,
                                                      @RequestBody @Validated WatchlistDTO dto) throws AccessDeniedException {
        var watchlist = getUserWatchlistOrThrow(userId, watchlistId);
        Watchlist updated = watchMapper.map(dto, Watchlist.class);
        updated.setId(watchlistId);
        updated.setUser(watchlist.getUser());
        watchlistRepository.save(updated);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deleta uma watchlist do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Watchlist deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Watchlist não encontrada", content = @Content)
    })
    @DeleteMapping("/{userId}/{watchlistId}")
    public ResponseEntity<Void> deleteWatchlistByUser(@PathVariable Long userId, @PathVariable Long watchlistId) throws AccessDeniedException {
        var watchlist = getUserWatchlistOrThrow(userId, watchlistId);
        watchlistRepository.delete(watchlist);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adiciona um filme à watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme adicionado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Watchlist ou Filme não encontrados", content = @Content)
    })
    @PatchMapping("/{userId}/{watchlistId}/add/{movieId}")
    public ResponseEntity<Void> addMovieToWatchlist(@PathVariable Long userId,
                                                    @PathVariable Long watchlistId,
                                                    @PathVariable Long movieId) throws AccessDeniedException {
        var watchlist = getUserWatchlistOrThrow(userId, watchlistId);
        var movie = getMovieOrThrow(movieId);
        if (!watchlist.containsMovie(Optional.ofNullable(movie))) {
            watchlist.addMovie(movie);
            watchlistRepository.save(watchlist);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove um filme da watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Watchlist ou Filme não encontrados", content = @Content)
    })
    @PatchMapping("/{userId}/{watchlistId}/remove/{movieId}")
    public ResponseEntity<Void> removeMovieFromWatchlist(@PathVariable Long userId,
                                                         @PathVariable Long watchlistId,
                                                         @PathVariable Long movieId) throws AccessDeniedException {
        var watchlist = getUserWatchlistOrThrow(userId, watchlistId);
        var movie = getMovieOrThrow(movieId);
        if (watchlist.containsMovie(Optional.ofNullable(movie))) {
            watchlist.removeMovie(movie);
            watchlistRepository.save(watchlist);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove todos os filmes da watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes removidos com sucesso"),
            @ApiResponse(responseCode = "404", description = "Watchlist não encontrada", content = @Content)
    })
    @PatchMapping("/{userId}/{watchlistId}/remove-all")
    public ResponseEntity<Void> removeAllMoviesFromWatchlist(@PathVariable Long userId,
                                                             @PathVariable Long watchlistId) throws AccessDeniedException {
        var watchlist = getUserWatchlistOrThrow(userId, watchlistId);
        watchlist.removeAllMovies();
        watchlistRepository.save(watchlist);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Marca um filme da watchlist como assistido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme marcado como assistido"),
            @ApiResponse(responseCode = "404", description = "Watchlist ou Filme não encontrados", content = @Content)
    })
    @PatchMapping("/{userId}/{watchlistId}/watched/{movieId}")
    public ResponseEntity<Void> markMovieAsWatched(@PathVariable Long userId,
                                                   @PathVariable Long watchlistId,
                                                   @PathVariable Long movieId) throws AccessDeniedException {
        var watchlist = getUserWatchlistOrThrow(userId, watchlistId);
        var movie = getMovieOrThrow(movieId);
        if (!watchlist.containsMovie(Optional.ofNullable(movie))) {
            throw new InvalidMovieStateException("O filme não está presente na watchlist");
        }

        if (watchlist.movieIsWatched(movieId)) {
            throw new InvalidMovieStateException("O filme já foi assistido");
        }

        watchlist.markMovieAsWatched(movie);
        watchlistRepository.save(watchlist);

        return ResponseEntity.ok().build();
    }

    private Watchlist getUserWatchlistOrThrow(Long userId, Long watchlistId) throws AccessDeniedException {
        var watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist não encontrada"));
        if (!watchlist.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Watchlist não pertence ao usuário");
        }
        return watchlist;
    }

    private Movie getMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
    }
}
