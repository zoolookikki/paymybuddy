package com.cordierlaurent.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.log4j.Log4j2;

/**
 * Controller handling errors related to Spring Security.
 */
@Controller
@Log4j2
public class SpringSecurityErrorController {

    /**
     * Handles access forbidden (403) errors generated by Spring Security (see SpringSecurityConfiguration).
     * <p>
     * This method is called when a user attempts to access a resource for which they do not have the necessary permissions.
     * It logs an error message and redirects the user to a custom error page.
     * </p>
     *
     * @param model The model for passing data to the Thymeleaf view.
     * @return The "myerror" view, displaying the access denied message.
     */
    @GetMapping("/error/403")
    public String accessDenied(Model model) {
        log.error("GetMapping/accessDenied");
        
        model.addAttribute("errorMessage", "Vous n'avez pas la permission d'accéder à cette page.");
        
        return "myerror"; 
    }
    
}
