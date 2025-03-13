package com.cordierlaurent.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.cordierlaurent.paymybuddy.model.User;
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
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    private static User userTest;
    private static Long userIdTest;

    @BeforeAll
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    void registerUserSuccessTest() {
        // given
        userTest = new User("John", "john@test.com", "John");

        // when
        Result result = userService.add(userTest);
        log.debug("registerUserSuccessTest,result="+result);
        
        // then
        assertThat(result.isSuccess()).isTrue();
        Optional<User> savedUser = userRepository.findByEmail(userTest.getEmail());
        assertThat(savedUser).isPresent();
        
        // id mémorisé pour les tests suivants.
        userIdTest = savedUser.get().getId(); 
    }
    
    @Test
    @Order(2)
    void getByRoleTest() {
        // given
        
        // when
        List<User> users = userService.getByRole("USER");
        
        // then
        assertThat(users).isNotEmpty();
        boolean emailFound = false;
        for (User user : users) {
            if (user.getEmail().equals(userTest.getEmail())) {
                emailFound = true;
                break;
            }
        }
        assertThat(emailFound).isTrue();
    }

    @Test
    @Order(3)
    void updateUserSuccessTest() {
        // given
        User updatedUser = new User();
        updatedUser.setName("Updated");
        updatedUser.setEmail("Updated@example.com");
        updatedUser.setPassword("Updated");

        // when
        boolean isUpdated = userService.update(userIdTest, updatedUser);
        
        // then
        assertThat(isUpdated).isTrue();
        // recherche de l'utilisateur mis à jour pour vérifier si tous les champs ont bien été modifiés.
        Optional<User> userFound = userRepository.findById(userIdTest);
        assertThat(userFound).isPresent();
        assertThat(userFound.get().getName()).isEqualTo("Updated");
        assertThat(userFound.get().getEmail()).isEqualTo("Updated@example.com");
        assertThat(passwordEncoder.matches("Updated", userFound.get().getPassword())).isTrue();
    }

    @Test
    @Order(4)
    void deleteUserSuccessTest() {
        // given
        
        // when
        boolean isDeleted = userService.delete(userIdTest);
        
        // then
        assertThat(isDeleted).isTrue();
        Optional<User> userFound = userRepository.findById(userIdTest);
        assertThat(userFound).isNotPresent();
    }
    
}
