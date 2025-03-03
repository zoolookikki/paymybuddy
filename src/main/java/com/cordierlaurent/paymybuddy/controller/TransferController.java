package com.cordierlaurent.paymybuddy.controller;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.ConnectionService;
import com.cordierlaurent.paymybuddy.service.TransactionService;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

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
        model.addAttribute("user", user);
        model.addAttribute("friends", connectionService.getFriends(user.getId()));
        model.addAttribute("transactions", transactionService.getUserTransactions(user.getId()));
        
        return "transfer"; 
    }
    
    @PostMapping("/transfer")
    public String processTransfer(
            @RequestParam Long friendId,
            @RequestParam String description,
            @RequestParam BigDecimal amount,
            Principal principal, 
            RedirectAttributes redirectAttributes) {
        log.debug("PostMapping/transfer,friendId="+friendId+",description="+description+",amount="+amount);
        
        User sender = userService.getAuthenticatedUser(principal);
        User receiver = userService.getById(friendId);

        Result result = transactionService.addTransaction(sender, receiver, description, amount);
        log.debug("PostMapping/transactionService.addTransaction,result="+result);
        
        /*
        return "redirect:/transfer" est une meilleure pratique que de faire cela car Spring MVC ne conserve pas les données du Model (même si cela implique une requête http supplémentaire).
        model.addAttribute("user", sender);
        model.addAttribute("friends", connectionService.getFriends(sender.getId()));
        model.addAttribute("transactions", transactionService.getUserTransactions(sender.getId()));        
        return "transfer";
        */        
        /*
        Comme Model ne persiste pas entre les requêtes, il faut donc utiliser RedirectAttributes pour passer des messages temporaires qui survivent à une redirection.
        */
        if (result.isSuccess()) {
            // model.addAttribute("successMessage", result.getMessage());
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            // model.addAttribute("errorMessage", result.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/transfer";
        
    }
    
}
