package com.cordierlaurent.paymybuddy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import static org.hamcrest.Matchers.containsString;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import com.cordierlaurent.paymybuddy.dto.TransactionRequestDTO;
import com.cordierlaurent.paymybuddy.model.Connection;
import com.cordierlaurent.paymybuddy.model.Transaction;
import com.cordierlaurent.paymybuddy.model.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TransferControllerIT extends AbstractIntegrationTest {

    private ResultActions performTransfer(TransactionRequestDTO transactionRequestDTO) throws Exception {
        return mockMvc.perform(post("/transfer")
                // Spring Security active la protection CSRF (Cross-Site Request Forgery) par défaut pour les requêtes POST, PUT, DELETE (jeton unique pour chaque session)
                .with(csrf())
                .param("receiverId", transactionRequestDTO.getReceiverId().toString())
                .param("description", transactionRequestDTO.getDescription())
                .param("amount", transactionRequestDTO.getAmount().toString()))
                // Affiche la requête et la réponse.
                .andDo(print()); 
    }
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("Successful transaction")
    void addTransactionSuccessTest() throws Exception {
        log.debug("addTransactionSuccessTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78", BigDecimal.valueOf(10.00));
        User user2 = saveUserTest("User2", "user2@test.com", "user2@78", BigDecimal.ZERO);
        log.debug("User1 enregistré avec ID : " + user1.getId());
        log.debug("User2 enregistré avec ID : " + user2.getId());
        connectionRepository.save(new Connection(user1, user2));
        TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(user2.getId(), "test", BigDecimal.valueOf(1.25));

        // when
        ResultActions resultActions = performTransfer(transactionRequestDTO);

        // then
        assertSuccess(resultActions, "transfer");

        // vérification des soldes
        Optional<User> updatedUser1 = userRepository.findById(user1.getId());
        assertThat(updatedUser1).isPresent();
        Optional<User> updatedUser2 = userRepository.findById(user2.getId());
        assertThat(updatedUser2).isPresent();
        log.debug("addTransactionSuccessTest,updatedUser1="+updatedUser1+",updatedUser2="+updatedUser2);

        assertThat(updatedUser1.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(8.75));
        assertThat(updatedUser2.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1.25));
        
        // vérification de la transaction.
        Transaction transaction = transactionRepository.findAll().get(0);
        assertThat(transaction).isNotNull();
        log.debug("addTransactionSuccessTest,transaction="+transaction);
        
        assertThat(transaction.getSender().getId()).isEqualTo(user1.getId());
        assertThat(transaction.getReceiver().getId()).isEqualTo(user2.getId());
        assertThat(transaction.getDescription()).isEqualTo("test");
        assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1.25));
    }
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("The transfer with a invalid amount fails")
    void addTransactionWhenAmountIsInvalidTest() throws Exception {
        log.debug("addTransactionWhenAmountIsInvalidTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78", BigDecimal.valueOf(10.00));
        User user2 = saveUserTest("User2", "user2@test.com", "user2@78", BigDecimal.ZERO);
        log.debug("User1 enregistré avec ID : " + user1.getId());
        log.debug("User2 enregistré avec ID : " + user2.getId());
        connectionRepository.save(new Connection(user1, user2));
        TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(user2.getId(), "test <= 0", BigDecimal.ZERO);

        // when
        ResultActions resultActions = performTransfer(transactionRequestDTO);

        // then
        assertFail(resultActions, "transfer", "transactionRequest", "amount");
    }
    
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("The transfer to itself fails")
    void addTransactionToItSelfFailTest() throws Exception {
        log.debug("addTransactionToItSelfFailTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78", BigDecimal.valueOf(10.00));
        TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(user1.getId(), "test", BigDecimal.valueOf(1.25));

        // when
        ResultActions resultActions = performTransfer(transactionRequestDTO);

        // then
        resultActions.andExpect(status().isOk())
        // On trappe car c'est une erreur grave de logique/programmation.
        .andExpect(view().name("myerror"))
        .andExpect(model().attributeExists("errorMessage"))
        .andExpect(model().attribute("errorMessage", containsString("Internal error")));
        
        // vérifier qu'aucune transaction n'a été enregistrée.
        assertThat(transactionRepository.count()).isZero();
    }
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("The transfer to a non-existent relationship fails")
    void addTransactionWhenReceiverIsNotAFriendFailTest() throws Exception {
        log.debug("addTransactionWhenReceiverIsNotAFriendFailTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78", BigDecimal.valueOf(10.00));
        User user2 = saveUserTest("User2", "user2@test.com", "user2@78", BigDecimal.ZERO);
        log.debug("User1 enregistré avec ID : " + user1.getId());
        log.debug("User2 enregistré avec ID : " + user2.getId());
        // pas de connections entre eux enregistrées.
        //connectionRepository.save(new Connection(user1, user2));
        TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(user2.getId(), "test", BigDecimal.valueOf(1.25));

        // when
        ResultActions resultActions = performTransfer(transactionRequestDTO);

        // then
        resultActions.andExpect(status().isOk())
        // On trappe car c'est une erreur grave de logique/programmation.
        .andExpect(view().name("myerror"))
        .andExpect(model().attributeExists("errorMessage"))
        .andExpect(model().attribute("errorMessage", containsString("Internal error")));
        
        // vérifier qu'aucune transaction n'a été enregistrée.
        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("The transfer with insufficient balance fails")
    void addTransactionWhenBalanceIsInsufficientTest() throws Exception {
        log.debug("addTransactionWhenBalanceIsInsufficientTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78", BigDecimal.valueOf(1.24));
        User user2 = saveUserTest("User2", "user2@test.com", "user2@78", BigDecimal.ZERO);
        log.debug("User1 enregistré avec ID : " + user1.getId());
        log.debug("User2 enregistré avec ID : " + user2.getId());
        connectionRepository.save(new Connection(user1, user2));
        TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(user2.getId(), "test", BigDecimal.valueOf(1.25));

        // when
        ResultActions resultActions = performTransfer(transactionRequestDTO);

        // then
        assertFail(resultActions, "transfer");
        // vérifier qu'aucune transaction n'a été enregistrée.
        assertThat(transactionRepository.count()).isZero();
    }
    
}

