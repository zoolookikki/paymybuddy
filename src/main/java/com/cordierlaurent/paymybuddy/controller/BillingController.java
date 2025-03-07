package com.cordierlaurent.paymybuddy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cordierlaurent.paymybuddy.dto.InvoiceDTO;
import com.cordierlaurent.paymybuddy.model.Transaction;
import com.cordierlaurent.paymybuddy.service.BillingService;
import com.cordierlaurent.paymybuddy.service.TransactionService;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/apitest/billing")
public class BillingController {

    @Autowired 
    TransactionService transactionService;

    @Autowired 
    BillingService billingService;

    @PostMapping("/{userId}")
    public ResponseEntity<String> createInvoice(@PathVariable Long userId) {
        log.info("PostMapping/createInvoice,userId="+userId);
        
        List<Transaction> transactions = transactionService.getUserTransactions(userId);

        if (transactions.isEmpty()) {
            log.warn("Aucune transaction trouvée pour l'utilisateur {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No transactions found for this user");
        }

        billingService.createInvoice(userId, transactions);
        return ResponseEntity.ok("Invoice created successfully");        
    }

    @GetMapping("/invoice/{invoiceId}")
    public InvoiceDTO getInvoice(@PathVariable Long invoiceId) {
        log.info("GetMapping/getInvoice,invoiceId="+invoiceId);
        
        return billingService.getInvoice(invoiceId);
    }

    @GetMapping("/user/{userId}")
    public List<InvoiceDTO> getInvoicesByUser(@PathVariable Long userId) {
        log.info("GetMapping/getInvoiceByUser,userId="+userId);
        
        return billingService.getInvoicesByUser(userId);
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long invoiceId) {
        log.info("DeleteMapping/deleteInvoice,invoiceId="+invoiceId);
        
        billingService.deleteInvoice(invoiceId);
        return ResponseEntity.ok("Invoice deletion successful");        
    }
    
}
