package com.cordierlaurent.paymybuddy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.model.Connection;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.ConnectionRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ConnectionService {

    @Autowired
    private ConnectionRepository connectionRepository;
    
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
    
    public List<Connection> getConnections(Long userId) {
        return connectionRepository.findByUserId(userId);
    }
    
}
