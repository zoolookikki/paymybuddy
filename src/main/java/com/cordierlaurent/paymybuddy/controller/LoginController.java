package com.cordierlaurent.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.log4j.Log4j2;

/**
 * Controller managing the display of login pages and user and administrator spaces.
 */
@Controller
@Log4j2
public class LoginController {

    /**
     * Displays the login page.
     *
     * @return The "login" view which displays the login form.
     */
    @GetMapping("/login")
    public String login() {
        log.debug("GetMapping/login");

        return "login"; 
    }
    
    /**
     * Displays the user's page after authentication.
     *
     * @return The "user" view which displays the user's dashboard.
     */
    @GetMapping("/user")
    public String getUser() {
        log.debug("GetMapping/user");

        return "user";
    }
    
    /**
     * Displays the administration page after authentication.
     *
     * @return The "admin" view which displays the administrator dashboard.
     */
    @GetMapping("/admin")
    public String getAdmin() {
        log.debug("GetMapping/admin");

        return "admin";
    }
    
}
