package com.cordierlaurent.paymybuddy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cordierlaurent.paymybuddy.dto.AdminTransactionDTO;
import com.cordierlaurent.paymybuddy.dto.UserTransactionDTO;
import com.cordierlaurent.paymybuddy.exception.TransactionException;
import com.cordierlaurent.paymybuddy.exception.UserNotFoundException;
import com.cordierlaurent.paymybuddy.model.Transaction;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.repository.TransactionRepository;
import com.cordierlaurent.paymybuddy.repository.UserRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
        
    @Autowired
    private ConnectionRepository connectionRepository;
    
    // Rollback automatique si une erreur se produit (simplifie énormément le code => voir TransactionTemplate (alternative Spring Boot) ou EntityManager (niveau le plus bas).
    @Transactional
    public Result addTransaction(User sender, User receiver, String description, BigDecimal amount) {
        log.debug("addTransaction,sender="+sender+",receiver="+receiver+",description="+description+",amount="+amount);
        
        // erreurs normalement contrôlées par le required du formulaire et le @Valid...
        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender and receiver must not be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        // erreurs de logique, de programmation.
        if (sender.getId().equals(receiver.getId())) {
            throw new TransactionException("Internal error : addTransaction : senderId = receiverId");
        }
        if (!connectionRepository.existsByUserIdAndFriendId(sender.getId(), receiver.getId())) {
            throw new TransactionException("Internal error : addTransaction : connection error : "+sender.getId()+ " "+receiver.getId());
        }   
        
        // erreurs utilisateur contrôlés par le service.
        if (sender.getBalance().compareTo(amount) < 0) {
            return new Result(false, "Votre solde de " +sender.getBalance()+  " € est insufisant"); 
        }

        // sauvegarde de la transaction.
        Transaction transaction = new Transaction();
        transaction.setSenderId(sender.getId());
        transaction.setReceiverId(receiver.getId());
        transaction.setDescription(description);
        transaction.setAmount(amount);

        // Mise à jour des soldes des utilisateurs.
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        // sauvegarde dans la base de données : d'abord la transaction. 
        transactionRepository.save(transaction);
        userRepository.save(sender);
        // pour tester @Transactional
/*
        if (true) { 
            throw new RuntimeException("Test @Transactional");
        }
*/        
        userRepository.save(receiver);
        
        return new Result (true, "La transaction de " + amount + " € a été effectuée");
    }
    
    public List<UserTransactionDTO> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findBySenderIdOrderByCreatedAtDesc(userId);
        List<UserTransactionDTO> transactionDTOs = new ArrayList<>();

        for (Transaction transaction : transactions) {
            // Trouver l'ami (receiver)
            User friend = userRepository.findById(transaction.getReceiverId())
                    .orElseThrow(() -> new UserNotFoundException("Internal error : getUserTransactions : "+transaction.getReceiverId()));

            UserTransactionDTO dto = new UserTransactionDTO(
                transaction.getCreatedAt(),
                friend.getName(),
                transaction.getDescription(),
                transaction.getAmount()
            );

            transactionDTOs.add(dto);
        }
        return transactionDTOs;
    }
    
    
    // pour Admin.
    public List<AdminTransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        List<AdminTransactionDTO> transactionDTOs = new ArrayList<>();

        for (Transaction transaction : transactions) {
            // Trouver le responsable de la transaction (sender)
            User user = userRepository.findById(transaction.getSenderId())
                    .orElseThrow(() -> new UserNotFoundException("Internal error : getAllTransactions/sender : "+transaction.getSenderId()));
            // Trouver l'ami (receiver)
            User friend = userRepository.findById(transaction.getReceiverId())
                    .orElseThrow(() -> new UserNotFoundException("Internal error : getAllTransactions/receiver : "+transaction.getReceiverId()));

            AdminTransactionDTO dto = new AdminTransactionDTO(
                transaction.getCreatedAt(),
                user.getName(),
                friend.getName(),
                transaction.getDescription(),
                transaction.getAmount()
            );

            transactionDTOs.add(dto);
        }
        
        return transactionDTOs;
    }
    
}
