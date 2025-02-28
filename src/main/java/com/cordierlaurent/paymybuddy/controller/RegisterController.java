package com.cordierlaurent.paymybuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class RegisterController {

    @Autowired
    private UserService userService;    

    @GetMapping("/register")
    public String displayRegistrationForm(Model model) {
        log.debug("GetMapping/register");
        // User = objet vide pour faire le lien avec le formulaire Thymeleaf.
        model.addAttribute("user", new User());
        // on affiche le formulaire d'inscription.
        return "register"; 
    }

    @PostMapping("/register")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // Model permet de transmettre des données de la couche serveur (Java) vers la vue (Thymeleaf).
    public String registerUser(@ModelAttribute User user, Model model) {
        log.debug("PostMapping/register");
        
        Result result = userService.add(user);
        
        if (result.isSuccess()) {
            model.addAttribute("successMessage", result.getMessage());
            return "login";
        }
        model.addAttribute("errorMessage", result.getMessage()); 
        return "register";
    }
    
}
