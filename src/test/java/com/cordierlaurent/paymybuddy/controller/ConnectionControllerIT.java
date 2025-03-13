package com.cordierlaurent.paymybuddy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import com.cordierlaurent.paymybuddy.dto.ConnectionRequestDTO;
import com.cordierlaurent.paymybuddy.model.Connection;
import com.cordierlaurent.paymybuddy.model.User;


import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConnectionControllerIT extends AbstractIntegrationTest {
    
    private ResultActions performConnection(ConnectionRequestDTO connectionRequestDTO) throws Exception {
        return mockMvc.perform(post("/connection")
                // Spring Security active la protection CSRF (Cross-Site Request Forgery) par défaut pour les requêtes POST, PUT, DELETE (jeton unique pour chaque session)
                .with(csrf())
                .param("email", connectionRequestDTO.getEmail()))
                // Affiche la requête et la réponse.
                .andDo(print()); 
    }

    @Test
    /*
    Avec @WithMockUser, Spring Security crée un utilisateur fictif qui est considéré comme identifié dans l'application.
    Obligatoire sinon le POST sera refusé. 
    */
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("Successfully adds a relationship")
    void addConnectionSuccessTest() throws Exception {
        log.debug("addConnectionSuccessTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78");
        User user2 = saveUserTest("User2", "user2@test.com", "user2@78");
        log.debug("User1 enregistré avec ID : " + user1.getId());
        log.debug("User2 enregistré avec ID : " + user2.getId());
        ConnectionRequestDTO connectionRequestDTO = new ConnectionRequestDTO("user2@test.com"); 

        // when
        ResultActions resultActions = performConnection(connectionRequestDTO);

        // then
        assertSuccess(resultActions, "connection");
        assertThat(connectionRepository.existsByUserIdAndFriendId(user1.getId(), user2.getId())).isTrue();
    }
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("Adding a relationship from an email that does not exist fails")
    void addConnectionFailWhenEmailDoesNotExistTest() throws Exception {
        log.debug("addConnectionFailWhenEmailDoesNotExistTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78");
        log.debug("User1 enregistré avec ID : " + user1.getId());
        ConnectionRequestDTO connectionRequestDTO = new ConnectionRequestDTO("user2@test.com"); 

        // when
        ResultActions resultActions = performConnection(connectionRequestDTO);

        // then
        assertFail(resultActions, "connection");
    }
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("Adding a relationship from the user's email itself fails")
    void addConectionFailWhenAddingYourselfTest() throws Exception {
        log.debug("addConectionFailWhenAddingYourselfTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78");
        log.debug("User1 enregistré avec ID : " + user1.getId());
        ConnectionRequestDTO connectionRequestDTO = new ConnectionRequestDTO("user1@test.com"); 

        // when
        ResultActions resultActions = performConnection(connectionRequestDTO);

        // then
        assertFail(resultActions, "connection");
    }
    
    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    @DisplayName("Adding a relationship with an already added email fails")
    void addConnectionFailWhenAddingExistingFriendTest() throws Exception {
        log.debug("addConnectionFailWhenAddingExistingFriendTest");
        // given
        User user1 = saveUserTest("User1", "user1@test.com", "user1@78");
        User user2 = saveUserTest("User2", "user2@test.com", "user2@78");
        log.debug("User1 enregistré avec ID : " + user1.getId());
        log.debug("User2 enregistré avec ID : " + user2.getId());
        connectionRepository.save(new Connection(user1, user2));
        ConnectionRequestDTO connectionRequestDTO = new ConnectionRequestDTO("user2@test.com"); 

        // when
        ResultActions resultActions = performConnection(connectionRequestDTO);

        // then
        assertFail(resultActions, "connection");
    }
    
}
