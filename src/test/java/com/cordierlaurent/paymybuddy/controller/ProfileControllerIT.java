package com.cordierlaurent.paymybuddy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import com.cordierlaurent.paymybuddy.dto.UpdateProfileRequestDTO;
import com.cordierlaurent.paymybuddy.model.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProfileControllerIT extends AbstractIntegrationTest {
    
    private ResultActions performProfile(UpdateProfileRequestDTO updateProfileRequestDTO) throws Exception {
        return mockMvc.perform(post("/profile")
                // Spring Security active la protection CSRF (Cross-Site Request Forgery) par défaut pour les requêtes POST, PUT, DELETE (jeton unique pour chaque session)
                .with(csrf())
                .param("name", updateProfileRequestDTO.getName())
                .param("email", updateProfileRequestDTO.getEmail())
                .param("password", updateProfileRequestDTO.getPassword()))
                // Affiche la requête et la réponse.
                .andDo(print()); 
    }

    @Test
    @WithMockUser(username = "john@test.com", roles = "USER")
    @DisplayName("Successfully update name and password profile")
    void updateProfileNameAndPasswordSuccessfullyTest() throws Exception {
        log.debug("updateProfileNameAndPasswordSuccessfullyTest");
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        // ici l'email n'a pas changé.
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO("JohnUpdated", "john@test.com", "Johnupdated@678"); 
        
        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);

        // then
        // si tout va bien la vue retournée est la page de profil.
        assertSuccess(resultActions, "profile");
        
        // vérifie que l'utilisateur a été modifié.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("updateProfileNameAndPasswordSuccessfullyTest,updatedUser="+updatedUser);
        assertThat(updatedUser.get().getName()).isEqualTo(updateProfileRequestDTO.getName());
        // ne pas contrôler le mot de passe car le cryptage sera toujours différent même si le mot de passe est identique.
       // assertThat(savedUser.get().getPassword()).isEqualTo(passwordEncoder.encode(updateProfileRequestDTO.getPassword()));
    }    

    // test à part pour la modification de l'email car dans ce cas de figure, on retourne sur le login.
    @Test
    @WithMockUser(username = "john@test.com", roles = "USER")
    @DisplayName("Successfully update email profile")
    void updateProfileEmailSuccessfullyTest() throws Exception {
        log.debug("updateProfileEmailSuccessfullyTest");
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        // ici uniquement l'email a changé.
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO("John", "johnupdated@test.com", "John@678"); 
        
        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);

        // then
        // On attend une redirection 302 vers "/login?emailChanged=true"
        // resultActions.andExpect(status().isOk())
        resultActions.andExpect(status().is3xxRedirection())
        // ici c'est la page de login car la modification de l'email nous oblige à nous reconnecter.
        .andExpect(redirectedUrl("/login?emailChanged=true"));
        
        // vérifie que l'utilisateur a été modifié.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("updateProfileEmailSuccessfullyTest=,savedUser="+updatedUser);
        assertThat(updatedUser.get().getEmail()).isEqualTo(updateProfileRequestDTO.getEmail());
    }    
    
    @ParameterizedTest(name = "Test {index}: name={0}, email={1}, password={2}")
    @WithMockUser(username = "john@test.com", roles = "USER")
    @DisplayName("Profile update fails when name is invalid")
    // Définit des valeurs d'entrée.
    @CsvSource({
        // Nom vide
        "'', 'john@test.com', 'John@678'", 
        // Nom avec des caractères invalides
        "'John++', 'john@test.com', 'John@678'" 
    })
    void profilNameIsInvalidTest(String name, String email, String password) throws Exception {
        log.debug("profilNameIsInvalidTest : " + name + " | " + email + " | " + password);
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO(name, email, password);

        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);
        
        // then
        assertFail(resultActions, "profile", "updateProfileRequest", "name");

        // vérifie que la modification n'a pas été enregistrée dans la base de données.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("profilNameIsInvalidTest,savedUser="+updatedUser);
        assertThat(updatedUser.get().getName()).isNotEqualTo(updateProfileRequestDTO.getName());
    }
    
    @Test
    @WithMockUser(username = "john@test.com", roles = "USER")
    @DisplayName("Profile update fails when name already exist")
    void profileEmaillAllreadyExistsTest() throws Exception {
        log.debug("profileNameAlreadyExistsTest");
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        saveUserTest("OtherJohn", "otherjohn@test.com", "OtherJohn@678");
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO("OtherJohn", "john@test.com", "John@678");

        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);

        // then
        assertFail(resultActions, "profile");
        
        // vérifie que la modification n'a pas été enregistrée dans la base de données.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("profileNameAlreadyExistsTest,savedUser="+updatedUser);
        assertThat(updatedUser.get().getName()).isNotEqualTo(updateProfileRequestDTO.getName());
    }       
    
    
    @ParameterizedTest
    @DisplayName("Profile update fails when invalid email values")
    @WithMockUser(username = "john@test.com", roles = "USER")
    @CsvSource({
        // email vide
        "'John', '', 'John@678'", 
        // email mal formé
        "'John', 'john@test', 'John@678'", 
        "'John', 'johntest.com', 'John@678'" 
    })
    void profileEmailIsInvalidTest(String name, String email, String password) throws Exception {
        log.debug("profileEmailIsInvalidTest : " + name + " | " + email + " | " + password);
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO(name, email, password);
        
        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);
        
        // then
        assertFail(resultActions, "profile", "updateProfileRequest", "email");
        
        // vérifie que la modification n'a pas été enregistrée dans la base de données.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("profileEmailIsInvalidTest,savedUser="+updatedUser);
        assertThat(updatedUser.get().getEmail()).isNotEqualTo(updateProfileRequestDTO.getEmail());
    }
    
    @Test
    @DisplayName("Profile update fails when email already exist")
    @WithMockUser(username = "john@test.com", roles = "USER")
    void profileEmailAlreadyExistsTest() throws Exception {
        log.debug("profileEmailAlreadyExistsTest");
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        saveUserTest("OtherJohn", "otherjohn@test.com", "OtherJohn@678");
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO("John", "otherjohn@test.com", "John@678");
        
        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);

        // then
        assertFail(resultActions, "profile");
        
        // vérifie que la modification n'a pas été enregistrée dans la base de données.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("profileEmailAlreadyExistsTest,savedUser="+updatedUser);
        assertThat(updatedUser.get().getEmail()).isNotEqualTo(updateProfileRequestDTO.getEmail());
    }

    @ParameterizedTest
    @DisplayName("Profile update fails when invalid email values")
    @WithMockUser(username = "john@test.com", roles = "USER")
    @CsvSource({
        // mot de passe trop court
        "'John', 'john@test.com', 'John@67'",
        // mot de passe sans caractère spécial
        "'John', 'john@test.com', 'John5678'", 
        // mot de passe sans majuscule
        "'John', 'john@test.com', 'john@678'", 
        // mot de passe sans chiffres
        "'John', 'john@test.com', 'John@John'" 
    })
    void profilePasswordIsInvalidTest(String name, String email, String password) throws Exception {
        log.debug("profilePasswordIsInvalidTest : " + name + " | " + email + " | " + password);
        // given
        User userToUpdate = saveUserTest("John", "john@test.com", "John@678");
        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO(name, email, password);

        // when
        ResultActions resultActions = performProfile(updateProfileRequestDTO);
        
        // then
        assertFail(resultActions, "profile", "updateProfileRequest", "password");
        
        // vérifie que la modification n'a pas été enregistrée dans la base de données.
        Optional<User> updatedUser = userRepository.findById(userToUpdate.getId());
        assertThat(updatedUser).isPresent();
        log.debug("profilePasswordIsInvalidTest,savedUser="+updatedUser);
        assertThat(updatedUser.get().getPassword()).isNotEqualTo(passwordEncoder.encode(updateProfileRequestDTO.getPassword()));
    }
    
}
