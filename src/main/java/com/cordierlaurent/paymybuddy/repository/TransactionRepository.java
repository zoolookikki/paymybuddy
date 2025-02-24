package com.cordierlaurent.paymybuddy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cordierlaurent.paymybuddy.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /*
    Méthodes utilisées déjà fournies par JpaRepository :    
        save(T entity)
    */
    
    /*
    Spring Data JPA va générer automatiquement => dérivation automatique des requêtes SQL à partir des noms de méthodes.
        findBySenderId => Sélectionne toutes les transactions où sender_id est égal à senderId.
        OrderByCreatedAtDesc => Trie les résultats par created_at en ordre décroissant.
        SELECT * FROM transactions WHERE sender_id = ? ORDER BY created_at DESC
    */
    List<Transaction> findBySenderIdOrderByCreatedAtDesc(Long senderId);
    /*
    Spring Data JPA va générer automatiquement => dérivation automatique des requêtes SQL à partir des noms de méthodes.
        findAllBy => Sélectionne toutes les transactions (équivalent à findAll()
        OrderByCreatedAtDesc => Trie les transactions par created_at en ordre décroissant.
        SELECT * FROM transactions ORDER BY created_at DESC
    */     
    List<Transaction> findAllByOrderByCreatedAtDesc();
}
