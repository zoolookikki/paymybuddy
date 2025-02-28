package com.cordierlaurent.paymybuddy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.ConnectionService;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
public class ConnectionController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connectionService;
    
    @GetMapping("/connection")
    public String displayConnectionForm(Principal principal, Model model) {
        log.debug("GetMapping/connection");

        return "connection"; 
    }    
    
    @PostMapping("/connection")
    public String addConnection(@RequestParam String email, Principal principal, Model model) {
        log.debug("PostMapping/connection,email="+email);
        
        // Récupère l'utilisateur connecté
        User user = userService.getAuthenticatedUser(principal);
        
        Result result = connectionService.add(user, email);

        if (result.isSuccess()) {
            model.addAttribute("successMessage", result.getMessage());
        } else {
            model.addAttribute("errorMessage", result.getMessage());
        }
        
        return "connection";
    }

}
