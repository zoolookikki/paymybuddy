package com.cordierlaurent.paymybuddy.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrentUserNotFoundException.class)
    public String handleUserNotFoundException(HttpServletRequest request, Model model, CurrentUserNotFoundException e) {
        // pour l'instant je laisse suite à différents tests mais je ne pense plus que ce soit utile car la redirection /logout buggue.
        if (request.getRequestURI().equals("/logout")) {
            log.debug("Ignorer CurrentUserNotFoundException car on est sur /logout");
            throw e; // Laisse Spring Security gérer l'erreur (redirigera vers /login)
        }
        log.debug("Interception de CurrentUserNotFoundException : {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage()); // Ajoute le message d'erreur à la vue
        return "error"; // Affiche error.html
    }
}
