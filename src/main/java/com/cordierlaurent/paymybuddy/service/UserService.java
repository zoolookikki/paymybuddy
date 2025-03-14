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

/**
 * User management service.
 * <p>
 * This service allows the creation, updating, and retrieval of users, as well as the validation of user data.
 * </p>
 *
 * @author [Ton Nom]
 * @version 1.0
 */
@Service
@Log4j2
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Retrieves the currently authenticated user.
     * <p>
     * This method uses Principal to retrieve the logged-in user's email address and searches for it in the database (email is in principal.getName())
     * </p>
     *
     * @param principal Contains the authenticated user's information.
     * @return The authenticated user.
     * @throws UserNotFoundException If the user does not exist (the method always returns the requested user otherwise it is considered an internal error).
     */
    public User getAuthenticatedUser(Principal principal) {
        // tests exceptions.
//      throw new IllegalArgumentException("ILLEGAL");
//      throw new UserNotFoundException("USERNOTFOUND");
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Internal error : getAuthenticatedUser ; "+principal.getName()));
    }
       
    /**
     * Validates a user's information before creation or update.
     *
     * @param user        The user to validate.
     * @param isUpdate    Indicates whether the operation is an update (true) or a create (false).
     * @param currentUser The user currently registered in the database (required in case of update).
     * @return A Result object indicating the success or failure of the validation.
     * @throws IllegalArgumentException If Objet User is invalid.
     */
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
    
    /**
     * Validates a user's information before creation.
     *
     * @param user The user to validate.
     * @return A Result object indicating the success or failure of the validation.
     */
    private Result userCreateValidation(User user) {
        return userValidation(user, false, null);
    }
    
    /**
     * Validates a user's information before updating it.
     *
     * @param user         The existing user in the database.
     * @param userToUpdate The user with the new values.
     * @return A Result object indicating the success or failure of the validation.
     */
    private Result userUpdateValidation(User user, User userToUpdate) {
        return userValidation(userToUpdate, true, user);
    }

    /**
     * Adds a new user after validation.
     *
     * @param user The user to add.
     * @return A Result object indicating the success or failure of the validation.
     */
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
    
    /**
     * Updates a user's information.
     *
     * @param user        The current user.
     * @param userToUpdate The user with the new values.
     * @return A Result object indicating the success or failure of the validation.
     */
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

    /**
     * Retrieves all users with a specific role.
     *
     * @param role The role of users to be recovered.
     * @return A list of users with this role.
     */
    public List<User> getByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    
    /**
     * Search for a user by email.
     *
     * @param email The email of the user you are looking for.
     * @return An Optional containing the user if it exists.
     */
    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The user ID.
     * @return The user corresponding to the provided ID.
     * @throws UserNotFoundException If the user does not exist (the method always returns the requested user otherwise it is considered an internal error).
     */
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Internal error: getById : " + id));
    }

}
