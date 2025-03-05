package com.cordierlaurent.paymybuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cordierlaurent.paymybuddy.dto.RegisterRequestDTO;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import jakarta.validation.Valid;
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
        
        // new ... = objet vide pour faire le lien avec le formulaire Thymeleaf.
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        
        // on affiche le formulaire.
        return "register"; 
    }

    @PostMapping("/register")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // @Valid pour valider tout ce qui a été déclaré comme à contrôler dans le DTO.
    // BindingResult est une interface qui sert à capturer et gérer les erreurs de validation lorsqu’un formulaire est soumis.
    // Model permet de transmettre des données de la couche serveur (Java) vers la vue (Thymeleaf).
    public String registerUser(@ModelAttribute("registerRequest") @Valid RegisterRequestDTO registerRequest, BindingResult bindingResult, Model model) {
        log.debug("PostMapping/register,registerRequest="+registerRequest);
        
        // pour afficher le formulaire avec les erreurs automatiquement.
        if (bindingResult.hasErrors()) {
            return "register"; 
        }
        
        Result result = userService.add(new User(registerRequest.getName(), registerRequest.getEmail(), registerRequest.getPassword()));
        log.info(registerRequest.getEmail()+"=>"+result.getMessage());
        
        if (result.isSuccess()) {
            model.addAttribute("successMessage", result.getMessage());
            return "login";
        }
        model.addAttribute("errorMessage", result.getMessage()); 
        
        return "register";
    }
    
}
