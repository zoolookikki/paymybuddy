package com.cordierlaurent.paymybuddy.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.UserRepository;

import lombok.extern.log4j.Log4j2;

/*
Ce service charge les utilisateurs depuis la base de données et les intégre à Spring Security.
Il implémente UserDetailsService, une interface de Spring Security qui permet d’authentifier les utilisateurs sur la base des informations contenues dans une base de données.
!! ATTENTION : La configuration de Spring Security doit prendre en compte cette classe via un AuthenticationManager (voir SpringSecurityConfiguration.java).
*/
@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    // UserRepository est un DAO (Data Access Object) qui permet de récupérer les utilisateurs en base de données.
    @Autowired
    private UserRepository userRepository;

    // Crée une liste d’autorités => Cette méthode transforme le rôle de l’utilisateur en une autorité compréhensible par Spring Security
    private List<SimpleGrantedAuthority> getGrantedAuthorities(String role) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return authorities;
    }

    // Fonction à implémenter utilisée par Spring Security pour charger un utilisateur.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Recherche de l'utilisateur par email = {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.debug("Aucun utilisateur trouvé");
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
        log.debug("Utilisateur trouvé : {}", user);

        /*
        Construit un objet User (de Spring Security) contenant :
            - Le nom d’utilisateur
            - Le mot de passe (hashé) 
            - Les rôles de l’utilisateur
        Le retour est un objet UserDetails, qui est une interface utilisée par Spring Security pour représenter un utilisateur authentifié.            
        */
       // new org.springframework.security.core.userdetails.User pour éviter confusion avec model.User.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getGrantedAuthorities(user.getRole())
        );
    }
}
