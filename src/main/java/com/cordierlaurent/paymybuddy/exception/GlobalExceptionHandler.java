package com.cordierlaurent.paymybuddy.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import lombok.extern.log4j.Log4j2;

/**
 * Global exception handler for the application.
 * <p>
 * This class catches all exceptions thrown by controllers and returns a custom error view with an explanatory message.
 * </p>
 * <p>
 * When an exception is caught:
 * <ul>
 *     <li>An error message is logged.</li>
 *     <li>The error message is added to the model for display in the error view.</li>
 *     <li>A custom error page is returned.</li>
 * </ul>
 */
@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    private String trap(Model model, String exceptionName, String message) {
        log.error(exceptionName + " : " + message);
        model.addAttribute("errorMessage", message);
        return "myerror";
    }

    @ExceptionHandler({
        UserNotFoundException.class,
        TransactionException.class,
        IllegalArgumentException.class,
        // 404 (page not found) => ne fonctionne pas même en mettant dans application.properties : spring.mvc.throw-exception-if-no-handler-found=true
        // elle arrive en exception générale voir ci-dessous...
        NoHandlerFoundException.class,
        DataAccessException.class,
        // par sécurité si oubli de gérer le BindingResult
        MethodArgumentNotValidException.class,
        HttpRequestMethodNotSupportedException.class,
        // la 404 (page not found) => arrive ici...  
        // Ici, on récupère tout le reste.
        Exception.class
    })
    public String handleExceptions(Model model, Exception e) {

        String exceptionName = e.getClass().getSimpleName();
        String message = (e.getMessage() != null) ? e.getMessage() : "Unknown error";

        // ici je préfère ne pas montrer la requête SQL générée à l'utilisateur.
        if (e instanceof DataAccessException) {
            message = "Database error"; 
        }
        // à reformater.
        else if (e instanceof MethodArgumentNotValidException) {
            /*            
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldError() != null 
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Invalid data";
            */                
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            FieldError fieldError = ex.getBindingResult().getFieldError();
            message = (fieldError != null && fieldError.getDefaultMessage() != null) 
                ? fieldError.getDefaultMessage() 
                : "Invalid data";
        } 
        return trap(model, exceptionName, message);
    }
}
