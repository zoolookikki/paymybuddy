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
import com.cordierlaurent.paymybuddy.model.Transaction;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.repository.TransactionRepository;
import com.cordierlaurent.paymybuddy.repository.UserRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

/**
 * Service enabling the management of transactions between users.
 * <p>
 * This service allows you to add transactions and retrieve the transaction history of a user or all transactions for administrative purposes.
 * </p>
 */
@Service
@Log4j2
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
        
    @Autowired
    private ConnectionRepository connectionRepository;
    
    /**
     * Adds a transaction between two users by handling balance checks and updates
     * <p>
     * This method is transactional: in case of error, all operations are canceled.
     * </p>
     *
     * @param sender      The user sending the money.
     * @param receiver    The user receiving the money.
     * @param description Description of the transaction.
     * @param amount      Transaction amount.
     * @return A Result object indicating the success or failure of the operation.
     * @throws IllegalArgumentException If any parameters are invalid (null, negative amount, empty description).
     * @throws TransactionException     If the user tries to send money to themselves or if the users are not connected to each other.
     */
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
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
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
    
    /**
     * Retrieves the history of transactions made by a user.
     *
     * @param userId The ID of the user whose transactions we want to retrieve.
     * @return A list of transactions sorted from newest to oldest.
     */
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findBySenderIdOrderByCreatedAtDesc(userId);
    }


    /**
     * Retrieves a user's transaction history and converts it into DTO objects for display.
     *
     * @param userId The user ID.
     * @return A list of UserTransactionDTOs containing transaction information.
     */
    public List<UserTransactionDTO> getUserTransactionsDTOs(Long userId) {
        List<Transaction> transactions = getUserTransactions(userId);
        List<UserTransactionDTO> transactionDTOs = new ArrayList<>();

        for (Transaction transaction : transactions) {
            UserTransactionDTO dto = new UserTransactionDTO(
                transaction.getCreatedAt(),
                transaction.getReceiver().getName(),
                transaction.getDescription(),
                transaction.getAmount()
            );

            transactionDTOs.add(dto);
        }
        return transactionDTOs;
    }
    
    
    /**
     * Retrieves all recorded transactions from all users (for administration only).
     *
     * @return A list of AdminTransactionDTOs containing information for all transactions, sorted from newest to oldest.
     */
    public List<AdminTransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        List<AdminTransactionDTO> transactionDTOs = new ArrayList<>();

        for (Transaction transaction : transactions) {
            AdminTransactionDTO dto = new AdminTransactionDTO(
                transaction.getCreatedAt(),
                transaction.getSender().getName(),
                transaction.getReceiver().getName(),
                transaction.getDescription(),
                transaction.getAmount()
            );

            transactionDTOs.add(dto);
        }
        
        return transactionDTOs;
    }
    
}
