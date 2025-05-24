package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import br.ifsp.film_catalog.model.common.BaseEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Setter
    private String name;

    @Setter
    @Column(unique = true)
    private String email;

    @Setter
    @Column(name = "password")
    private String password;

    @Setter
    @Column(unique = true)
    private String username;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Watchlist> watchlists = new HashSet<>();

    public void addWatchlist(Watchlist watchlist) {
        this.watchlists.add(watchlist);
        watchlist.setUser(this); // Set the back-reference
    }

    public void removeWatchlist(Watchlist watchlist) {
        this.watchlists.remove(watchlist);
        watchlist.setUser(null); // Remove the back-reference
    }

}
