package com.cordierlaurent.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cordierlaurent.paymybuddy.model.Transaction;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.repository.TransactionRepository;
import com.cordierlaurent.paymybuddy.repository.UserRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
//Permet de charger automatiquement application-test.properties.
@ActiveProfiles("test") 
//Empêche Spring de forcer H2.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/*
JUnit crée une seule instance de la classe de test pour toutes les méthodes de test.
@BeforeAll et @AfterAll n'ont plus besoin d'être static !!!
*/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@Log4j2
public class TransactionServiceIT {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ConnectionService connectionService;

    private User user;
    private User friend;
    private User other;

    private User createUser(String name, String email, String password, BigDecimal amount) {
        User userToCreate = new User(name, email, password);
        userToCreate.setBalance(amount);
        userService.add(userToCreate);
        // pour récupérer le user (avec son id autoincrémenté).
        return userRepository.findByEmail(userToCreate.getEmail()).orElseThrow();
    }
    
    private void checkThatNoTransactionExists(Result result) {
        assertThat(result.isSuccess()).isFalse();
        List<Transaction> transactions = transactionService.getAllTransactions();
        assertThat(transactions).isEmpty();
    }

    @BeforeAll
    void setUp() {
        transactionRepository.deleteAll();
        connectionRepository.deleteAll();
        userRepository.deleteAll();

        user = createUser("User", "user@test.com", "User", BigDecimal.TWO);
        log.debug("user="+user);
        friend = createUser("Friend", "friend@test.com", "Friend", BigDecimal.ZERO);
        log.debug("friend="+friend);
        other = createUser("Other", "other@test.com", "Other", BigDecimal.TEN);
        log.debug("other="+friend);
        Result result = connectionService.add(user, friend);
        log.debug("result="+result);
        assertThat(result.isSuccess()).isTrue();
    }
    
    @Test
    @Order(1)
    void addYourSelfFailTest() {
        // when
        Result result = transactionService.addTransaction(user, user, "addYourSelfFailTest", BigDecimal.ONE);
        log.debug("addYourSelfFailTest,result="+result);
        
        // then
        checkThatNoTransactionExists(result) ;
    }
    
    @Test
    @Order(2)
    void addToNonFriendFailTest() {
        // when
        Result result = transactionService.addTransaction(user, other, "addToNonFriendFailTest", BigDecimal.ONE);
        log.debug("addToNonFriendFailTest,result="+result);
        
        //then
        checkThatNoTransactionExists(result) ;
    }   
    
    @Test
    @Order(3)
    void addWithInsufficientBalanceFailTest() {
        // when
        Result result = transactionService.addTransaction(user, friend, "addWithInsufficientBalanceFailTest", BigDecimal.valueOf(100));
        log.debug("addWithInsufficientBalanceFailTest,result="+result);
        
        // then
        checkThatNoTransactionExists(result) ;
    }    
    
    @Test
    @Order(4)
    void addSuccessTest() {
        // given.
        String transactionDescription = "addSuccessTest";
        BigDecimal amountToTransfer = BigDecimal.valueOf(1.25);
        BigDecimal expectedUserBalance = user.getBalance().subtract(amountToTransfer);
        BigDecimal expectedFriendBalance = friend.getBalance().add(amountToTransfer);        
        
        // when
        Result result = transactionService.addTransaction(user, friend, transactionDescription, amountToTransfer);
        log.debug("addSuccessTest,result="+result);

        // then
        assertThat(result.isSuccess()).isTrue();
        // vérifier que la transaction est correctement engistrée.
        List<Transaction> transactions = transactionRepository.findBySenderIdOrderByCreatedAtDesc(user.getId());
        log.debug("addSuccessTest,transactions="+transactions);
        
        assertThat(transactions).isNotEmpty().hasSize(1);
        Transaction transaction = transactions.getFirst();
        assertThat(transaction.getAmount()).isEqualByComparingTo(amountToTransfer);
        assertThat(transaction.getDescription()).isEqualTo(transactionDescription);
        assertThat(transaction.getSenderId()).isEqualTo(user.getId());
        assertThat(transaction.getReceiverId()).isEqualTo(friend.getId());
        // vérifier les soldes des utilisateurs concernés.
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        User updatedFriend = userRepository.findById(friend.getId()).orElseThrow();
        log.debug("addSuccessTest,updatedUser="+updatedUser+",updatedFriend="+updatedFriend);
        assertThat(updatedUser.getBalance()).isEqualByComparingTo(expectedUserBalance);
        assertThat(updatedFriend.getBalance()).isEqualByComparingTo(expectedFriendBalance);        
    }    
        
}
