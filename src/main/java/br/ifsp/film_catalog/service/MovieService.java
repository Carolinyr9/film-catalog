package br.ifsp.film_catalog.service;

import br.ifsp.film_catalog.config.AdminUserInitializer;
import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.InvalidMovieStateException;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.ReviewRepository;
import br.ifsp.film_catalog.repository.GenreRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MovieService {

    private final AdminUserInitializer adminUserInitializer;

    private GenreRepository genreRepository;
    private MovieRepository movieRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final ReviewRepository reviewRepository;

    public MovieService(GenreRepository genreRepository, MovieRepository movieRepository,
                        ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper, AdminUserInitializer adminUserInitializer, ReviewRepository reviewRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.adminUserInitializer = adminUserInitializer;

        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getAllMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public MovieResponseDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
        return modelMapper.map(movie, MovieResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getMoviesByTitle(String title, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        return pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getMoviesByGenre(String genreName, Pageable pageable) {
        Genre genre = genreRepository.findByNameIgnoreCase(genreName)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found: " + genreName));
        Page<Movie> moviePage = movieRepository.findByGenresContaining(genre, pageable);
        return pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getMoviesByReleaseYear(int year, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByReleaseYear(year, pageable);
        return pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class);
    }

    @Transactional
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        if (movieRepository.findByTitle(movieRequestDTO.getTitle()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Movie with title '" + movieRequestDTO.getTitle() + "' already exists.");
        }

        Movie movie = modelMapper.map(movieRequestDTO, Movie.class);
        for (Genre genre : movieRequestDTO.getGenres()) {
            Genre existingGenre = genreRepository.findByNameIgnoreCase(genre.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Genre not found: " + genre.getName()));
            movie.addGenre(existingGenre);
        }
        Movie savedMovie = movieRepository.save(movie);
        return modelMapper.map(savedMovie, MovieResponseDTO.class);
    }

    @Transactional
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO movieRequestDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));

        movieRepository.findByTitleIgnoreCase(movieRequestDTO.getTitle()).ifPresent(existingMovie -> {
            if (!existingMovie.getId().equals(id)) {
                throw new InvalidMovieStateException("Another movie with this title and year already exists");
            }
        });
        
        modelMapper.map(movieRequestDTO, movie);
        Movie updatedMovie = movieRepository.save(movie);
        return modelMapper.map(updatedMovie, MovieResponseDTO.class);
    }

    @Transactional
    public MovieResponseDTO patchMovie(Long id, MoviePatchDTO patchDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie id not found: " + id));

        if (patchDTO.getTitle() != null) {
            movie.setTitle(patchDTO.getTitle());
        }
        if (patchDTO.getSynopsis() != null) {
            movie.setSynopsis(patchDTO.getSynopsis());
        }
        if (patchDTO.getReleaseYear() != null) {
            movie.setReleaseYear(patchDTO.getReleaseYear());
        }
        if (patchDTO.getDuration() != null) {
            movie.setDuration(patchDTO.getDuration());
        }
        if (patchDTO.getContentRating() != null) {
            try {
                ContentRating rating = ContentRating.valueOf(patchDTO.getContentRating().toUpperCase());
                movie.setContentRating(rating);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid content rating value: " + patchDTO.getContentRating());
            }
        }

        if (patchDTO.getGenreIds() != null) {
            // Aqui você vai precisar buscar os gêneros pelo IDs e setar no filme
            List<Genre> genres = genreRepository.findAllById(patchDTO.getGenreIds());
            movie.getGenres().clear();
            movie.getGenres().addAll(genres);
        }

        movie = movieRepository.save(movie);
        return modelMapper.map(movie, MovieResponseDTO.class);
    }


    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie id not found: " + id);
        }
        // Add checks here if movie deletion has other constraints (e.g., part of watchlists, reviews)

        movieRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getHighlightedMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "generalScore"));
        Page<Movie> topMovies = reviewRepository.findTopRatedMovies(pageable);

        if (topMovies.isEmpty()) {
            pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
            topMovies = movieRepository.findAll(pageable);
        }

        List<MovieResponseDTO> movieDTOs = topMovies.getContent().stream()
            .map(movie -> modelMapper.map(movie, MovieResponseDTO.class))
            .toList();

        return new PagedResponse<>(
            movieDTOs,
            topMovies.getNumber(),
            topMovies.getSize(),
            topMovies.getTotalElements(),
            topMovies.getTotalPages(),
            topMovies.isLast()
        );
    }


}