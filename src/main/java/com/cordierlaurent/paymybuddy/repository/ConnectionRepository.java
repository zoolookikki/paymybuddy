package com.cordierlaurent.paymybuddy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cordierlaurent.paymybuddy.model.Connection;

/*
Pas besoin de @Repository sur une interface JpaRepository
Spring le détecte automatiquement et crée un bean sans configuration supplémentaire.
*/
//@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> { 
    /*
    Méthodes utilisées déjà fournies par JpaRepository :    
        findById(Long id)
        existsById(Long id)
        save(T entity)
        deleteById(Long id);
    */
    // Spring Data génère automatiquement la requête : SELECT COUNT(*) FROM connections WHERE user_id = ? AND friend_id = ? et vérifie COUNT => false/true
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    // Spring Data génère automatiquement la requête : SELECT * FROM connections WHERE user_id =
    List<Connection> findByUserId(Long userId);
}
