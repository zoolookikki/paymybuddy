package com.cordierlaurent.paymybuddy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cordierlaurent.paymybuddy.model.Transaction;
import com.cordierlaurent.paymybuddy.service.TransactionService;

//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
public class AdminController {

    @Autowired 
    TransactionService transactionService;
    
    // .......................partie admin pour tests.................................
    @GetMapping("/admin/transactions")
    public String displayTransactions(Model model) {
        List<Transaction> transactions = transactionService.getAllTransactions();
        // pour test affichage vide.
        //transactions.clear();
        // permet d’ajouter à Model un objet de mon choix.
        model.addAttribute("transactions", transactions);
        // Renvoie vers la page admin_transactions.html
        return "admin_transactions"; 
    }

}
