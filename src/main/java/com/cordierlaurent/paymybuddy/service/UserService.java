package com.cordierlaurent.paymybuddy.service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.exception.UserNotFoundException;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.repository.UserRepository;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // méthode qui renvoit si elle fonctionne l'utilisateur courant (sinon c'est une erreur interne grave).
    public User getAuthenticatedUser(Principal principal) {
        // tests exceptions.
//      throw new IllegalArgumentException("ILLEGAL");
//      throw new UserNotFoundException("USERNOTFOUND");
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Internal error : getAuthenticatedUser ; "+principal.getName()));
    }
        
    private Result userValidation(User user, boolean isUpdate, User currentUser) {
        log.debug("userValidation,user="+user+",isUpdate="+isUpdate+",currentUser="+currentUser);

        // erreurs normalement contrôlées par le required du formulaire et le @Valid...
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isUpdate && (user.getPassword() == null || user.getPassword().isEmpty())) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (isUpdate && currentUser == null) {
            throw new IllegalArgumentException("Internal error : userValidation");
        }

        // erreurs utilisateur contrôlés par le service.
        if ((!isUpdate || !user.getName().equals(currentUser.getName())) && userRepository.findByName(user.getName()).isPresent()) {
            return new Result(false, "Le nom " + user.getName() + " est déjà utilisé"); 
        }
        if ((!isUpdate || !user.getEmail().equals( currentUser.getEmail())) && userRepository.findByEmail(user.getEmail()).isPresent()) {
            return new Result(false, "L'email " + user.getEmail() + " est déjà utilisé");
        }

        return new Result(true, "OK");
    }
    
    private Result userCreateValidation(User user) {
        return userValidation(user, false, null);
    }
    
    private Result userUpdateValidation(User user, User userToUpdate) {
        return userValidation(userToUpdate, true, user);
    }

    public Result add(User user) {
        log.debug("add,user="+user);
        
        Result validationResult = userCreateValidation(user);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }  
        
        // e-mail toujours en minuscule
        user.setEmail(user.getEmail().trim().toLowerCase());
        // encodage du mot de passe.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        userRepository.save(user);
        
        return new Result(true,"Votre inscription a réussi");
    }
    
    public Result update(User user, User userToUpdate) {
        log.debug("update,user="+user+",userToUpdate="+userToUpdate);

        if (user.getName().equals(userToUpdate.getName()) &&
            user.getEmail().equals(userToUpdate.getEmail()) &&
            (userToUpdate.getPassword() == null || userToUpdate.getPassword().isEmpty())) {
            return new Result(false, "Vous n'avez rien modifié");
        }

        Result validationResult = userUpdateValidation(user, userToUpdate);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }  
        

        user.setName(userToUpdate.getName());
        // e-mail toujours en minuscule
        user.setEmail(userToUpdate.getEmail().trim().toLowerCase());
        // ne modifier que si il a été saisit.
        if (userToUpdate.getPassword() != null && !userToUpdate.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userToUpdate.getPassword()));
        }
        
        userRepository.save(user);
        return new Result(true, "Votre profil a été mis à jour avec succès");
    }

    public List<User> getByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // méthode qui renvoit si elle fonctionne l'utilisateur demandé (sinon c'est une erreur interne grave).
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Internal error: getById : " + id));
    }

}
