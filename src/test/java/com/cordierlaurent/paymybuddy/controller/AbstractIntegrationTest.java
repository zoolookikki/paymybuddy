package com.cordierlaurent.paymybuddy.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.repository.TransactionRepository;
import com.cordierlaurent.paymybuddy.repository.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;

@SpringBootTest
//Charge MockMvc avec SpringBootTest
@AutoConfigureMockMvc
//Permet de charger automatiquement application-test.properties.
@ActiveProfiles("test") 
//Empêche Spring de forcer H2.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Log4j2
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ConnectionRepository connectionRepository;

    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    @Autowired
    protected EntityManager entityManager;

    @BeforeEach
    void setup() {
        // Attention ici problème de suppression à cause de l'intégrité => solution : deleteAllInBatch (pas en mémoire) + clear.
//      transactionRepository.deleteAll();
//      connectionRepository.deleteAll();
//      userRepository.deleteAll();
        transactionRepository.deleteAllInBatch();
        connectionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Nettoie le cache Hibernate pour éviter les accès à des entités supprimées.
        entityManager.clear();
    }

    protected User saveUserTest(String name, String email, String password) {
        return userRepository.save(new User(name, email, passwordEncoder.encode(password)));
    }

    protected User saveUserTest(String name, String email, String password, BigDecimal balance) {
        User user = new User(name, email, passwordEncoder.encode(password));
        user.setBalance(balance);
        return userRepository.save(user);
    }
    
    protected void assertSuccess(ResultActions resultActions, String viewName) throws Exception {
        resultActions.andExpect(status().isOk())
            .andExpect(view().name(viewName))
            .andExpect(model().attributeExists("successMessage"));
    }

    protected void assertFail(ResultActions resultActions, String viewName) throws Exception {
        resultActions.andExpect(status().isOk())
            .andExpect(view().name(viewName))
            // ici, c'est un contrôle effectué par le service donc on se base sur l'existence de cet attribut.
            .andExpect(model().attributeExists("errorMessage"));
    }
    
    protected void assertFail(ResultActions resultActions, String viewName, String attributeName, String fieldName) throws Exception {
        resultActions.andExpect(status().isOk())
            .andExpect(view().name(viewName))
            .andExpect(model().attributeHasFieldErrors(attributeName, fieldName));
    }
    
}
