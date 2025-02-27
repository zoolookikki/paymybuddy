package com.cordierlaurent.paymybuddy.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Result add(User user) {
        log.debug("add,user="+user);
        
        // cas impossibles car required sur le formulaire d'inscription mais par sécurité.
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return new Result(false, "Le nom est requis");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return new Result(false, "L'email est requis");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return new Result(false, "Le mot de passe est requis");
        }

        
        if (!user.getName().matches("^[A-Za-zÀ-ÖØ-öø-ÿ0-9 -]+$")) {
            return new Result(false, "Le nom ne doit contenir que des lettres, chiffres, espaces ou tirets");
        }
        if (userRepository.findByName(user.getName()).isPresent()) {
            return new Result(false, "Ce nom est déjà utilisé"); 
        }
        
        // normalement déjà contrôlé par le type=email sur le formulaire.
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            return new Result(false, "Format d'email invalide.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return new Result(false, "Cet e-mail a déjà été enregistré"); 
        }
        
        if (!user.getPassword().matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            return new Result(false, "Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial");
        }
  
        // e-mail toujours en minuscule
        user.setEmail(user.getEmail().trim().toLowerCase());
        // encodage du mot de passe.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        userRepository.save(user);
        
        return new Result(true,"Votre inscription a réussi");
    }
    
    public List<User> getByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
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
