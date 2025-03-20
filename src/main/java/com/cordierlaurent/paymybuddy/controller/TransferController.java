package com.cordierlaurent.paymybuddy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cordierlaurent.paymybuddy.dto.TransactionRequestDTO;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.ConnectionService;
import com.cordierlaurent.paymybuddy.service.TransactionService;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

/**
 * Controller managing money transfers between users.
 * <p>
 * This controller allows users to view their balance, transactions, and transfer money to their friends via a Thymeleaf form.
 * </p>
 */
@Controller
@Log4j2
public class TransferController {

    @Autowired
    private UserService userService;  
        
    @Autowired
    private ConnectionService connectionService;
    
    @Autowired
    private TransactionService transactionService;
   
    /**
     * Displays the money transfer form.
     * <p>
     * This method retrieves the logged-in user's information, their friends list, and their transaction history to display on the transfer page.
     * </p>
     *
     * @param principal Contains the authenticated user's information.
     * @param model The model for passing data to the Thymeleaf view.
     * @return The "transfer" view displaying the transfer form.
     */
    @GetMapping("/transfer")
    public String displayTransferForm(Principal principal, Model model) {
        log.debug("GetMapping/transfer");

        User user = userService.getAuthenticatedUser(principal);
        
        // new ... = objet vide pour faire le lien avec le formulaire Thymeleaf.
        model.addAttribute("transactionRequest", new TransactionRequestDTO());

        model.addAttribute("user", user);
        model.addAttribute("friends", connectionService.getFriends(user.getId()));
        model.addAttribute("transactions", transactionService.getUserTransactionsDTOs(user.getId()));

        return "transfer"; 
    }
    
    
    /**
     * Process a money transfer.
     * <p>
     * This method handles the transfer form submission. It validates the entered data, verifies that the user has sufficient funds, and saves the transaction to the database.
     * If the transfer is successful, a confirmation message is displayed.
     * Otherwise, an error message is returned.
     * </p>
     * <p>
     * </p>
     *
     * @param transactionRequestDTO Transaction details provided by the user.
     * @param bindingResult  Result of form validation.
     * @param principal Contains the authenticated user's information.
     * @param model The model for passing data to the Thymeleaf view.
     * @return The "transfer" view is updated with new data and confirmation or error messages.
     */
    @PostMapping("/transfer")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // @Valid pour valider tout ce qui a été déclaré comme à contrôler dans le DTO.
    // BindingResult est une interface qui sert à capturer et gérer les erreurs de validation lorsqu’un formulaire est soumis.
    public String processTransfer(
            @ModelAttribute("transactionRequest") @Valid TransactionRequestDTO transactionRequest,
            BindingResult bindingResult,
            Principal principal, 
            Model model) { 
        log.debug("PostMapping/transfer,transactionRequest="+transactionRequest);
        
        User sender = userService.getAuthenticatedUser(principal);
        
        // toujours ajouter au moins le user sinon Thymeleaf plante en cas d'erreur sur la validation @Valid.
        model.addAttribute("user", sender);

        // pour afficher le formulaire avec les erreurs automatiquement.
        if (bindingResult.hasErrors()) {
            return "transfer"; 
        }

        User receiver = userService.getById(transactionRequest.getReceiverId());

        Result result = transactionService.addTransaction(sender, receiver, transactionRequest.getDescription(), transactionRequest.getAmount());
        log.info(sender.getEmail()+"=>"+result.getMessage());
        
        if (result.isSuccess()) {
            model.addAttribute("successMessage", result.getMessage());
            model.addAttribute("transactionRequest", new TransactionRequestDTO());
        } else {
            model.addAttribute("errorMessage", result.getMessage());
        }

        // Cette partie est à recharger.
        model.addAttribute("user", sender);
        model.addAttribute("friends", connectionService.getFriends(sender.getId()));
        model.addAttribute("transactions", transactionService.getUserTransactionsDTOs(sender.getId()));
        
        return "transfer";
        
    }
    
}
