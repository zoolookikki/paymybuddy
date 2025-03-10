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

@Controller
@Log4j2
public class ConnectionController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ConnectionService connectionService;
    
    @GetMapping("/connection")
    public String displayConnectionForm(Model model) {
        log.debug("GetMapping/connection");
        
        model.addAttribute("connectionRequest", new ConnectionRequestDTO());

        return "connection"; 
    }    
    
    @PostMapping("/connection")
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
           // Réinitialisation du formulaire
            model.addAttribute("connectionRequest", new ConnectionRequestDTO()); 
        } else {
            model.addAttribute("errorMessage", result.getMessage());
        }
        
        return "connection";
    }

}
