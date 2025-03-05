package com.cordierlaurent.paymybuddy.service;

import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cordierlaurent.paymybuddy.model.Connection;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.repository.UserRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
//Permet de charger automatiquement application-test.properties.
@ActiveProfiles("test") 
// Empêche Spring de forcer H2.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/*
JUnit crée une seule instance de la classe de test pour toutes les méthodes de test.
@BeforeAll et @AfterAll n'ont plus besoin d'être static !!!
*/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@Log4j2
public class ConnectionServiceIT {
    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    private User user;
    private User friend;
    private User other;
    
    private User createUser(String name, String email, String password) {
        User userToCreate = new User(name, email, password);
        userService.add(userToCreate);
        // pour récupérer le user (avec son id autoincrémenté).
        return userRepository.findByEmail(userToCreate.getEmail()).orElseThrow();
    }
    
    @BeforeAll
    void setUp() {
        connectionRepository.deleteAll();
        userRepository.deleteAll(); 

        user = createUser("User", "user@test.com", "User");
        log.debug("user="+user);
        friend = createUser("Friend", "friend@test.com", "Friend");
        log.debug("friend="+friend);
        other = createUser("Other", "other@test.com", "Other");
        log.debug("other="+friend);
    }
    
    @Test
    @Order(1)
    void addYourselfFailTest() {
        // when
        Result result = connectionService.add(user, user);
        log.debug("addYourselfTest,result="+result);
        
        // then
        assertThat(result.isSuccess()).isFalse();
    }
    @Test
    @Order(2)
    void addSuccessTest() {
        // when
        Result result = connectionService.add(user, friend);
        log.debug("addSuccessTest,result="+result);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(connectionRepository.existsByUserIdAndFriendId(user.getId(), friend.getId())).isTrue();
    }
    
    @Test
    @Order(3)
    void addDuplicateFailTest() {
        // When
        Result result = connectionService.add(user, friend);
        log.debug("addDuplicateFailTest,result="+result);

        // THEN
        assertThat(result.isSuccess()).isFalse();
    }
    
    @Test
    @Order(4)
    void getConnectionsTest() {
        // given.
        // on ajoute la relation inverse (qui ne doit pas compter).
        connectionService.add(friend, user);
        // puis une autre avec un 2 ème utilisateur.
        connectionService.add(user, other);

        // when
        List<Connection> connections = connectionService.getConnections(user.getId());
        log.debug("'getConnectionsTest="+connections);

        // then
        assertThat(connections).isNotEmpty();
        assertThat(connections).hasSize(2);
// A REVOIR.        
//        assertThat(connections.get(0).getFriendId()).isEqualTo(friend.getId());
//        assertThat(connections.get(1).getFriendId()).isEqualTo(other.getId());
    }
    
}
