package com.cordierlaurent.paymybuddy.controller;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


import com.cordierlaurent.paymybuddy.dto.RegisterRequestDTO;
import com.cordierlaurent.paymybuddy.model.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RegisterControllerIT extends AbstractIntegrationTest {

    private void verifyUserRegistration(RegisterRequestDTO registerRequestDTO, boolean mustExist) {
        Optional<User> savedUser = userRepository.findByEmail(registerRequestDTO.getEmail());

        if (mustExist) {
            assertThat(savedUser).isPresent();
            log.debug("verifyUserRegistration,savedUser="+savedUser);
            assertThat(savedUser.get().getName()).isEqualTo(registerRequestDTO.getName());
            assertThat(savedUser.get().getEmail()).isEqualTo(registerRequestDTO.getEmail());            
        } else {
            log.debug("verifyUserRegistration,savedUser isEmpty");
            assertThat(savedUser).isEmpty();
        }
    }
    
    private ResultActions performRegistration(RegisterRequestDTO registerRequestDTO) throws Exception {
        return mockMvc.perform(post("/register")
                // Spring Security active la protection CSRF (Cross-Site Request Forgery) par défaut pour les requêtes POST, PUT, DELETE (jeton unique pour chaque session)
                .with(csrf())
                .param("name", registerRequestDTO.getName())
                .param("email", registerRequestDTO.getEmail())
                .param("password", registerRequestDTO.getPassword()))
                // Affiche la requête et la réponse.
                .andDo(print()); 
    }
    
    @Test
    @DisplayName("Check that a user who registers is properly registered")
    void successTest() throws Exception {
        log.debug("successTest");
        // given
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("John", "john@test.com", "John@678");

        // when
        ResultActions resultActions = performRegistration(registerRequestDTO);

        // then
        // si tout va bien la vue retournée est la page de login.
        assertSuccess(resultActions, "login");
        // vérifie que l'utilisateur a été enregistré dans la base de données.
        verifyUserRegistration(registerRequestDTO, true);
    }

    // Permet d'exécuter le test plusieurs fois avec des entrées différentes.
    @ParameterizedTest(name = "Test {index}: name={0}, email={1}, password={2}")
    @DisplayName("Check that a registration with invalid name values fails")
    // Définit des valeurs d'entrée.
    @CsvSource({
        // Nom vide
        "'', 'john@test.com', 'John@678'", 
        // Nom avec des caractères invalides
        "'John++', 'john@test.com', 'John@678'" 
    })
    void registerNameIsInvalidTest(String name, String email, String password) throws Exception {
        log.debug("registerNameIsInvalidTest : " + name + " | " + email + " | " + password);
        // given
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(name, email, password);
        
        // when
        ResultActions resultActions = performRegistration(registerRequestDTO);

        // then
        // on boucle sur la page d'inscription.
        // vérifie spécifiquement si le champ "name" a une erreur sur cette DTO (contrôlé par @Valid)
        // ATTENTION ici c'est "registerRequest" et non "registerRequestDTO" car c'est le nom qui a été donné ici dans le contrôleur : @ModelAttribute("registerRequest")
        assertFail(resultActions, "register", "registerRequest", "name");
        // vérifie que l'utilisateur n'a pas été enregistré dans la base de données.
        verifyUserRegistration(registerRequestDTO, false);
    }
    
    @Test
    @DisplayName("Check that a registration fail when name already exist")
    void registerNameAlreadyExistsTest() throws Exception {
        log.debug("registerNameAlreadyExistsTest");
        // given
        saveUserTest("John", "john@test.com", "John@678");
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("John", "otherjohn@test.com", "OtherJohn@678");

        // when
        ResultActions resultActions = performRegistration(registerRequestDTO);

        // then
        assertFail(resultActions, "register");
        verifyUserRegistration(registerRequestDTO, false);
    }       
    
    @ParameterizedTest
    @DisplayName("Check that a registration with invalid email values fails")
    @CsvSource({
        // email vide
        "'John', '', 'John@678'", 
        // email mal formé
        "'John', 'john@test', 'John@678'", 
        "'John', 'johntest.com', 'John@678'" 
    })
    void registerEmailIsInvalidTest(String name, String email, String password) throws Exception {
        log.debug("registerEmailIsInvalidTest : " + name + " | " + email + " | " + password);
        // given
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(name, email, password);
        
        // when
        ResultActions resultActions = performRegistration(registerRequestDTO);

        // then
        assertFail(resultActions, "register", "registerRequest", "email");
        verifyUserRegistration(registerRequestDTO, false);
    }
    
    @Test
    @DisplayName("A registration with email already exist")
    void registerEmailAlreadyExistsTest() throws Exception {
        log.debug("registerEmailAlreadyExistsTest");
        // given
        saveUserTest("John", "john@test.com", "John@678");
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("OtherJohn", "john@test.com", "otherJohn@678");

        // when
        ResultActions resultActions = performRegistration(registerRequestDTO);

        // then
        assertFail(resultActions, "register");
    }
    
    @ParameterizedTest
    @DisplayName("A registration with invalid password values fails")
    @CsvSource({
        // mot de passe vide
        "'John', 'john@test.com', ''", 
        // mot de passe trop court
        "'John', 'john@test.com', 'John@67'",
        // mot de passe sans caractère spécial
        "'John', 'john@test.com', 'John5678'", 
        // mot de passe sans majuscule
        "'John', 'john@test.com', 'john@678'", 
        // mot de passe sans chiffres
        "'John', 'john@test.com', 'John@John'" 
    })
    void registerPasswordIsInvalidTest(String name, String email, String password) throws Exception {
        log.debug("registerPasswordIsInvalidTest : " + name + " | " + email + " | " + password);
        // given
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(name, email, password);
        
        // when
        ResultActions resultActions = performRegistration(registerRequestDTO);

        // then
        assertFail(resultActions, "register", "registerRequest", "password");
        verifyUserRegistration(registerRequestDTO, false);
    }
    
    
}
