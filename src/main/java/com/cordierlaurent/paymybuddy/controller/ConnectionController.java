package com.cordierlaurent.paymybuddy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cordierlaurent.paymybuddy.dto.ConnectionRequestDTO;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.ConnectionService;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

/**
 * Controller managing the addition of connections between users.
 * <p>
 * This controller displays the connection addition form and processes requests to add a relationship between users.
 * </p>
 */
@Controller
@Log4j2
public class ConnectionController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ConnectionService connectionService;
    
    /**
     * Displays the connection management form.
     * <p>
     * This method initializes a ConnectionRequestDTO object and adds it to the model for use in the Thymeleaf form.
     * </p>
     *
     * @param model The model for passing data to the Thymeleaf view.
     * @return The name of the Thymeleaf view.
     */
    @GetMapping("/connection")
    public String displayConnectionForm(Model model) {
        log.debug("GetMapping/connection");
        
        model.addAttribute("connectionRequest", new ConnectionRequestDTO());

        return "connection"; 
    }    
    
    /**
     * Handles adding a new connection between users.
     * <p>
     * This method checks the validity of the form, retrieves the logged in user, and adds the login.
     * </p>
     *
     * @param connectionRequestDTO Contains the email of the user to add as a login.
     * @param bindingResult Result of form validation.
     * @param principal Contains the authenticated user's information.
     * @param model The model for passing data to the Thymeleaf view.
     * @return The view updated with a success or error message.
     */
    @PostMapping("/connection")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // @Valid pour valider tout ce qui a été déclaré comme à contrôler dans le DTO.
    // BindingResult est une interface qui sert à capturer et gérer les erreurs de validation lorsqu’un formulaire est soumis.
    public String addConnection(
            @ModelAttribute("connectionRequest") @Valid ConnectionRequestDTO connectionRequest,
            BindingResult bindingResult,
            Principal principal, 
            Model model) {
        log.debug("PostMapping/connection,connectionRequest="+connectionRequest);
        
        if (bindingResult.hasErrors()) {
            return "connection";
        }
        
        // Récupère l'utilisateur connecté
        User user = userService.getAuthenticatedUser(principal);
        
        Result result = connectionService.add(user, connectionRequest.getEmail());
        log.info(user.getEmail()+"=>"+result.getMessage());

        if (result.isSuccess()) {
            model.addAttribute("successMessage", result.getMessage());
           // Réinitialisation du formulaire (plus ergonomique).
            model.addAttribute("connectionRequest", new ConnectionRequestDTO()); 
        } else {
            model.addAttribute("errorMessage", result.getMessage());
        }
        
        return "connection";
    }

}
