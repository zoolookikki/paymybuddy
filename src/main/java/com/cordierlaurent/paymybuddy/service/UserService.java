package com.cordierlaurent.paymybuddy.service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.exception.CurrentUserNotFoundException;
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

    // méthode qui renvoit si elle fonctionne l'utilisateur courant (sauf erreur interne grave).
    public User getAuthenticatedUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CurrentUserNotFoundException("Erreur interne : utilisateur non trouvé"));
    }
    
    private Result userValidation(User user, boolean isUpdate, User currentUser) {
        log.debug("userValidation,user="+user+",isUpdate="+isUpdate+",currentUser="+currentUser);
        if (isUpdate && currentUser == null)
            throw new CurrentUserNotFoundException("Erreur interne : validation impossible");
        
        // cas impossibles car required sur le formulaire d'inscription mais par sécurité.
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return new Result(false, "Le nom est requis");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return new Result(false, "L'email est requis");
        }
        if (!isUpdate && (user.getPassword() == null || user.getPassword().isEmpty())) {
            return new Result(false, "Le mot de passe est requis");
        }

            
        if (!user.getName().matches("^[A-Za-zÀ-ÖØ-öø-ÿ0-9 -]+$")) {
            return new Result(false, "Le nom ne doit contenir que des lettres, chiffres, espaces ou tirets");
        }
        if ((!isUpdate || !user.getName().equals(currentUser.getName())) && userRepository.findByName(user.getName()).isPresent()) {
            return new Result(false, "Le nom " + user.getName() + " est déjà utilisé"); 
        }

        // normalement déjà contrôlé par le type=email sur le formulaire.
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            return new Result(false, "Format d'e-mail invalide.");
        }
        if ((!isUpdate || !user.getEmail().equals( currentUser.getEmail())) && userRepository.findByEmail(user.getEmail()).isPresent()) {
            return new Result(false, "L'email " + user.getEmail() + " est déjà utilisé");
        }

        if (!isUpdate || (user.getPassword() != null && !user.getPassword().isEmpty())) {
            if (!user.getPassword().matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
                return new Result(false, "Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial");
            }
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


    // Anciennes fonctions à garder pour le moment.
    public boolean update(Long id, User userToUpdate) {
        Optional<User> userFound = userRepository.findById(id);
        if (userFound.isPresent()) {
            User userToSave = userFound.get();
            // Mettre à jour uniquement si le champ n'est pas vide
            if (userToUpdate.getName() != null && !userToUpdate.getName().trim().isEmpty()) {
                userToSave.setName(userToUpdate.getName());
            }
            if (userToUpdate.getEmail() != null && !userToUpdate.getEmail().trim().isEmpty()) {
                userToSave.setEmail(userToUpdate.getEmail());
            }
            if (userToUpdate.getPassword() != null && !userToUpdate.getPassword().trim().isEmpty()) {
                userToSave.setPassword(passwordEncoder.encode(userToUpdate.getPassword()));
            }
            userRepository.save(userToSave);
            return true;
        }
        return false;
    }
    public boolean delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
