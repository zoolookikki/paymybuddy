package com.cordierlaurent.paymybuddy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.model.Connection;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.repository.UserRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ConnectionService {

    @Autowired
    private ConnectionRepository connectionRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
        
    public Result add(User user, String email) {
        log.debug("ConnectionService.add,userId="+user.getId()+",email="+email);

        // contrôlé par le formulaire.
        if (email == null || email.trim().isEmpty()) {
            return new Result(false, "L'email est requis");
        }
        email = email.trim().toLowerCase();
        // normalement déjà contrôlé par le type=email sur le formulaire.
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            return new Result(false, "Format d'e-mail invalide.");
        }
        
        Optional<User> optionalFriend = userService.getByEmail(email);
        if (optionalFriend.isEmpty()) {
            return new Result(false, "L'utilisateur " + email + " n'existe pas"); 
        }
        // pour simplification ensuite.
        User friend = optionalFriend.get();
        
        if (user.getId().equals(friend.getId())) {
            return new Result(false, "Vous ne pouvez pas vous ajouter vous-même"); 
        }

        if (connectionRepository.existsByUserIdAndFriendId(user.getId(), friend.getId())) {
            return new Result(false, "Vous avez déjà ajouté l'utilisateur "+friend.getEmail());
        }

        Connection connection = new Connection();
        connection.setUserId(user.getId());
        connection.setFriendId(friend.getId());
        connectionRepository.save(connection);

        // optionnel : ajout de la relation inverse (friend vers user)
        // ATTENTION SI MISE EN ROUTE ==> IL FAUT FAIRE UN @Transactional sur cette fonction.
        /*
        Connection reverseConnection = new Connection();
        connection.setUserId(friend.getId());
        connection.setFriendId(user.getId());
        connectionRepository.save(reverseConnection);
        */

        return new Result(true,"L'utilisateur " + email + " a été ajouté");
    }
    
    public List<Connection> getConnections(Long userId) {
        return connectionRepository.findByUserId(userId);
    }
    
    public List<User> getFriends(Long userId) {
        log.debug("getFriends,userId="+userId);
        
        // recherche la liste des amis de cet utilisateur.
        List<Connection> connections = connectionRepository.findByUserId(userId);
        List<Long> friendIds = new ArrayList<>();
        for (Connection connection : connections) {
            friendIds.add(connection.getFriendId());
        }
        // trouve tous les utilisateurs amis avec la liste des id amis
        return userRepository.findAllById(friendIds);
    }
    
    // Anciennes fonctions à garder pour le moment.
    public Result add(User user, User friend) {
        if (user == null || friend == null) {
            throw new IllegalArgumentException("User and friend must not be null");
        }        
        
        log.debug("ConnectionService.add,userId="+user.getId()+",friendId="+friend.getId());
        if (user.getId().equals(friend.getId())) {
            return new Result(false, "You cannot add yourself"); 
        }

        if (connectionRepository.existsByUserIdAndFriendId(user.getId(), friend.getId())) {
            return new Result(false, "You have already added this user");
        }

        Connection connection = new Connection();
        connection.setUserId(user.getId());
        connection.setFriendId(friend.getId());
        connectionRepository.save(connection);

        // optionnel : ajout de la relation inverse (friend vers user)
        // ATTENTION SI MISE EN ROUTE ==> IL FAUT FAIRE UN @Transactional sur cette fonction.
        /*
        Connection reverseConnection = new Connection();
        connection.setUserId(friend.getId());
        connection.setFriendId(user.getId());
        connectionRepository.save(reverseConnection);
        */

        return new Result(true,"Your friend is registered");
    }    
    
}
