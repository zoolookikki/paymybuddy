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

//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class TransferController {

    @Autowired
    private UserService userService;  
        
    @Autowired
    private ConnectionService connectionService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/transfer")
    public String displayTransferForm(Principal principal, Model model) {
        log.debug("GetMapping/transfer");

        User user = userService.getAuthenticatedUser(principal);
        
        // new ... = objet vide pour faire le lien avec le formulaire Thymeleaf.
        model.addAttribute("transactionRequest", new TransactionRequestDTO());

        model.addAttribute("user", user);
        model.addAttribute("friends", connectionService.getFriends(user.getId()));
        model.addAttribute("transactions", transactionService.getUserTransactions(user.getId()));

        return "transfer"; 
    }
    
    @PostMapping("/transfer")
    public String processTransfer(
            @ModelAttribute("transactionRequest") @Valid TransactionRequestDTO transactionRequest,
            BindingResult bindingResult,
            Principal principal, 
            Model model) { 
        log.debug("PostMapping/transfer,transactionRequest="+transactionRequest);
        
        // pour afficher le formulaire avec les erreurs automatiquement.
        if (bindingResult.hasErrors()) {
            return "transfer"; 
        }

        User sender = userService.getAuthenticatedUser(principal);
        User receiver = userService.getById(transactionRequest.getReceiverId());

        Result result = transactionService.addTransaction(sender, receiver, transactionRequest.getDescription(), transactionRequest.getAmount());
        log.debug("PostMapping/transactionService.addTransaction,result="+result);
        
        if (result.isSuccess()) {
            model.addAttribute("successMessage", result.getMessage());
            model.addAttribute("transactionRequest", new TransactionRequestDTO());
        } else {
            model.addAttribute("errorMessage", result.getMessage());
        }

        // Cette partie est à recharger.
        model.addAttribute("user", sender);
        model.addAttribute("friends", connectionService.getFriends(sender.getId()));
        model.addAttribute("transactions", transactionService.getUserTransactions(sender.getId()));
        
        return "transfer";
        
    }
    
}
