package com.cordierlaurent.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.UserRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    private static User userTest;
    private static Long userIdTest;

    /*
    Injection du UserRepository en paramètre avec @Autowired => permet de contourner la limitation statique :
        - @BeforeAll est une fonction statique qui si on utilise un attribut partagé (ici userRepository), nécessite d'être déclaré en statique.
        - Mais les champs injectés avec @Autowired ne peuvent pas être statiques, car Spring injecte des beans dans des instances, pas dans des classes statiques.
    */
    @BeforeAll
    static void setup(@Autowired UserRepository userRepository) {
        // suppression de l'utilisateur de test si il existe.
        Optional<User> user = userRepository.findByEmail("john@test.com");
        if (user.isPresent()) {
            userRepository.delete(user.get());
        }
    }

    @Test
    @Order(1)
    void registerUserSuccessTest() {
        // given
        userTest = new User("John", "john@test.com", "John");

        // when
        boolean isRegistered = userService.add(userTest);
        
        // then
        assertThat(isRegistered).isTrue();
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
