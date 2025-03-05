package com.cordierlaurent.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.log4j.Log4j2;

// @RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
// @Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class LoginController {

    @GetMapping("/login")
    public String login() {
        log.debug("GetMapping/login");

        return "login"; 
    }
    
    @GetMapping("/user")
    public String getUser() {
        log.debug("GetMapping/user");

        return "user";
    }
    
    @GetMapping("/admin")
    public String getAdmin() {
        log.debug("GetMapping/admin");

        return "admin";
    }
    
}
