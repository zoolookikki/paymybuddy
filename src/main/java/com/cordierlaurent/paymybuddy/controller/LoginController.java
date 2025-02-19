package com.cordierlaurent.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// @RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
// @Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        // Redirige vers login.html dans /templates
        return "login"; 
    }
    
    @GetMapping("/user")
    public String getUser() {
        // Redirige vers user.html dans /templates
        return "user";
    }
    
    @GetMapping("/admin")
    public String getAdmin() {
        // Redirige vers admin.html dans /templates
        return "admin";
    }
    
}
