package com.cordierlaurent.paymybuddy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cordierlaurent.paymybuddy.model.User;

/*
Pas besoin de @Repository sur une interface JpaRepository
Spring le détecte automatiquement et crée un bean sans configuration supplémentaire.
*/
//@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data génère automatiquement la requête : SELECT * FROM users WHERE name = ?
    Optional<User> findByName(String name);
    // Spring Data génère automatiquement la requête : SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    // Spring Data génère automatiquement la requête : SELECT * FROM users WHERE role = ?
    List<User> findByRole(String role);
}
