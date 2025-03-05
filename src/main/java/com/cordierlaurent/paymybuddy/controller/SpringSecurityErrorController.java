package com.cordierlaurent.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
public class SpringSecurityErrorController {

    // erreur provenant de Spring Security (voir SpringSecurityConfiguration)
    @GetMapping("/error/403")
    public String accessDenied(Model model) {
        log.error("GetMapping/accessDenied");
        
        model.addAttribute("errorMessage", "Vous n'avez pas la permission d'accéder à cette page.");
        
        return "myerror"; 
    }
    
}
