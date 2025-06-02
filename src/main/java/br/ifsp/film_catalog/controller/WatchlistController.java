package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.WatchlistRequestDTO;
import br.ifsp.film_catalog.dto.WatchlistResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Watchlists", description = "API para gerenciamento de watchlists de usuários")
@Validated
@RestController
@RequestMapping("/api/users/{userId}/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @Operation(summary = "Criar uma nova watchlist para o usuário especificado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Watchlist criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<WatchlistResponseDTO> createWatchlist(
            @PathVariable Long userId,
            @Valid @RequestBody WatchlistRequestDTO watchlistRequestDTO) {
        WatchlistResponseDTO createdWatchlist = watchlistService.createWatchlist(userId, watchlistRequestDTO);
        return new ResponseEntity<>(createdWatchlist, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todas as watchlists do usuário especificado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlists recuperadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<PagedResponse<WatchlistResponseDTO>> getWatchlistsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        PagedResponse<WatchlistResponseDTO> watchlists = watchlistService.getWatchlistsByUser(userId, pageable);
        return ResponseEntity.ok(watchlists);
    }

    @Operation(summary = "Obter uma watchlist específica do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlist recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Watchlist ou Usuário não encontrado")
    })
    @GetMapping("/{watchlistId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<WatchlistResponseDTO> getWatchlistByIdAndUser(
            @PathVariable Long userId,
            @PathVariable Long watchlistId) {
        WatchlistResponseDTO watchlist = watchlistService.getWatchlistByIdAndUser(userId, watchlistId);
        return ResponseEntity.ok(watchlist);
    }

    @Operation(summary = "Atualizar uma watchlist do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlist atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Watchlist ou Usuário não encontrado")
    })
    @PutMapping("/{watchlistId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<WatchlistResponseDTO> updateWatchlist(
            @PathVariable Long userId,
            @PathVariable Long watchlistId,
            @Valid @RequestBody WatchlistRequestDTO watchlistRequestDTO) {
        WatchlistResponseDTO updatedWatchlist = watchlistService.updateWatchlist(userId, watchlistId, watchlistRequestDTO);
        return ResponseEntity.ok(updatedWatchlist);
    }

    @Operation(summary = "Deletar uma watchlist do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Watchlist deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Watchlist ou Usuário não encontrado")
    })
    @DeleteMapping("/{watchlistId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<Void> deleteWatchlist(
            @PathVariable Long userId,
            @PathVariable Long watchlistId) {
        watchlistService.deleteWatchlist(userId, watchlistId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adicionar um filme a uma watchlist do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme adicionado à watchlist com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Watchlist, Usuário ou Filme não encontrado")
    })
    @PostMapping("/{watchlistId}/movies/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<WatchlistResponseDTO> addMovieToWatchlist(
            @PathVariable Long userId,
            @PathVariable Long watchlistId,
            @PathVariable Long movieId) {
        WatchlistResponseDTO updatedWatchlist = watchlistService.addMovieToWatchlist(userId, watchlistId, movieId);
        return ResponseEntity.ok(updatedWatchlist);
    }

    @Operation(summary = "Remover um filme de uma watchlist do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme removido da watchlist com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Watchlist, Usuário ou Filme não encontrado")
    })
    @DeleteMapping("/{watchlistId}/movies/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<WatchlistResponseDTO> removeMovieFromWatchlist(
            @PathVariable Long userId,
            @PathVariable Long watchlistId,
            @PathVariable Long movieId) {
        WatchlistResponseDTO updatedWatchlist = watchlistService.removeMovieFromWatchlist(userId, watchlistId, movieId);
        return ResponseEntity.ok(updatedWatchlist);
    }
}
