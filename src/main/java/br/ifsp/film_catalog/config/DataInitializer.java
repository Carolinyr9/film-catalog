package br.ifsp.film_catalog.config; // A 'config' or 'util' package is a good place

import br.ifsp.film_catalog.model.*;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private WatchlistRepository watchlistRepository;

    @Override
    @Transactional // Ensures all operations run within a single transaction
    public void run(String... args) throws Exception {
        System.out.println("--- STARTING DATA INITIALIZATION ---");

        // 1. Create Roles and Genres (Lookup data)
        Role userRole = roleRepository.save(new Role(RoleName.ROLE_USER));
        Role adminRole = roleRepository.save(new Role(RoleName.ROLE_ADMIN));
        Genre actionGenre = genreRepository.save(new Genre("Action"));
        Genre sciFiGenre = genreRepository.save(new Genre("Sci-Fi"));

        // 2. Create a new User and assign a Role
        User user = new User();
        user.setUsername("testuser");
        user.addRole(userRole);
        user.addRole(adminRole); // Testing many-to-many
        userRepository.save(user);

        // 3. Create a Movie and assign Genres
        Movie movie1 = new Movie();
        movie1.setTitle("Inception");
        movie1.addGenre(actionGenre);
        movie1.addGenre(sciFiGenre);
        movieRepository.save(movie1);
        
        Movie movie2 = new Movie();
        movie2.setTitle("The Matrix");
        movie2.addGenre(actionGenre);
        movie2.addGenre(sciFiGenre);
        movieRepository.save(movie2);

        // 4. Create a Watchlist for the User
        Watchlist mySciFiList = new Watchlist("My Sci-Fi Favorites", user);
        
        // 5. Add Movies to the Watchlist (using the helper method)
        mySciFiList.addMovie(movie1);
        mySciFiList.addMovie(movie2);
        
        // 6. Save the watchlist (cascade will handle the user link)
        watchlistRepository.save(mySciFiList);
        
        // 7. Verification: Fetch and print data
        System.out.println("\n--- VERIFICATION ---");
        Watchlist fetchedList = watchlistRepository.findById(mySciFiList.getId()).get();
        System.out.println("Watchlist Name: " + fetchedList.getName());
        System.out.println("Owned by User: " + fetchedList.getUser().getUsername());
        System.out.println("Movies in this list:");
        fetchedList.getMovies().forEach(movie -> {
            System.out.println("- " + movie.getTitle());
        });

        System.out.println("\n--- DATA INITIALIZATION COMPLETE ---");
    }
}