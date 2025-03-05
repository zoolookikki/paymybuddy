package com.cordierlaurent.paymybuddy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cordierlaurent.paymybuddy.dto.AdminTransactionDTO;
import com.cordierlaurent.paymybuddy.service.TransactionService;

import lombok.extern.log4j.Log4j2;

//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class AdminController {

    @Autowired 
    TransactionService transactionService;
    
    // .......................partie admin pour tests.................................
    @GetMapping("/admin/transactions")
    public String displayAdminTransactions(Model model) {
        log.debug("GetMapping/admin/transactions");
        
        List<AdminTransactionDTO> transactions = transactionService.getAllTransactions();
        // pour test affichage vide.
        //transactions.clear();
        
        // permet d’ajouter à Model un objet de mon choix.
        model.addAttribute("transactions", transactions);
        
        // Renvoie vers la page admin_transactions.html
        return "admin_transactions"; 
    }

}
