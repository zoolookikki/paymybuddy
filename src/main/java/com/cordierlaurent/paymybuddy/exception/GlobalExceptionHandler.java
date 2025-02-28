package com.cordierlaurent.paymybuddy.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(HttpServletRequest request, Model model, UserNotFoundException e) {
        log.error("UserNotFoundException interception : {}", e.getMessage());
        // pour l'instant je laisse suite à différents tests mais je ne pense plus que ce soit utile car la redirection /logout buggue.
        if (request.getRequestURI().equals("/logout")) {
            log.debug("Ignorer UserNotFoundException car on est sur /logout");
            throw e; // Laisse Spring Security gérer l'erreur (redirigera vers /login)
        }
        model.addAttribute("errorMessage", e.getMessage()); 
        return "myerror"; 
    }
    
    @ExceptionHandler(TransactionException.class)
    public String handleTransactionException(Model model, TransactionException e) {
        log.error("TransactionException interception : {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage()); 
        return "myerror"; 
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(Model model, IllegalArgumentException e) {
        log.error("IllegalArgumentException interception : {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage()); 
        return "myerror"; 
    }
    
}
