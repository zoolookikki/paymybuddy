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

/**
 * Controller managing user registration.
 * <p>
 * This controller allows new users to register via a form.
 * It displays the registration form, handles data validation, and adds the user to the system. 
 * If successful, the user is redirected to the login page.
 * </p>
 */
//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class RegisterController {

    @Autowired
    private UserService userService;    

    /**
     * Displays the registration form.
     * <p>
     * This method is called when a user accesses the form.
     * It prepares an empty RegisterRequestDTO object to bind the fields of the Thymeleaf form.
     * </p>
     *
     * @param model  The model for passing data to the Thymeleaf view.
     * @return The "register" view containing the registration form.
     */    
    @GetMapping("/register")
    public String displayRegistrationForm(Model model) {
        log.debug("GetMapping/register");
        
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        
        return "register"; 
    }

    /**
     * Manages the submission of the registration form.
     * <p>
     * This method is called when the user submits the form.
     * It validates the entered data, attempts to add the user, and displays a success message or an error message depending on the result.
     * </p>
     *
     * @param registerRequestDTO Contains information entered by the user (name, email, password).
     * @param bindingResult  Result of form validation.
     * @param model The model for passing data to the Thymeleaf view.
     * @return The "login" view if successful, or "register" if an error occurred.
     */    
    @PostMapping("/register")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // @Valid pour valider tout ce qui a été déclaré comme à contrôler dans le DTO.
    // BindingResult est une interface qui sert à capturer et gérer les erreurs de validation lorsqu’un formulaire est soumis.
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
