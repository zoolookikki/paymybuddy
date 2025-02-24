package com.cordierlaurent.paymybuddy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Result addTransaction(User sender, User receiver, String description, BigDecimal amount) {
        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender and receiver must not be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        if (sender.getId().equals(receiver.getId())) {
            return new Result(false, "You cannot transfer money to yourself"); 
        }
        if (!connectionRepository.existsByUserIdAndFriendId(sender.getId(), receiver.getId())) {
            return new Result (false, "You can only send money to your friends");
        }        
        if (sender.getBalance().compareTo(amount) < 0) {
            return new Result(false, "Your balance is insufficient to complete this transaction"); 
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
        userRepository.save(receiver);
        
        return new Result (true, "Successful transaction");
    }
    
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findBySenderIdOrderByCreatedAtDesc(userId);
    }
    
    // pour Admin.
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }
    
}
