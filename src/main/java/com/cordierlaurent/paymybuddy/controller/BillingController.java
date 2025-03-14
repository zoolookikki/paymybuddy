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

/**
 * REST controller for billing management.
 * <p>
 * This controller allows you to generate invoices from a user's transactions, retrieve individual or user-specific invoices, and delete existing invoices.
 * </p>
 */
@RestController
@Log4j2
@RequestMapping("/apitest/billing")
public class BillingController {

    @Autowired 
    TransactionService transactionService;

    @Autowired 
    BillingService billingService;
    
    /**
     * Creates an invoice for a given user based on their transactions.
     * <p>
     * This method retrieves a user's transactions based on their ID, then generates an associated invoice if any transactions are found.
     * </p>
     *
     * @param userId The ID of the user to create an invoice for.
     * @return A message indicating the success or failure of the creation.
     */
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

    /**
     * Retrieves a specific invoice based on its ID.
     *
     * @param invoiceId The ID of the invoice to be retrieved.
     * @return InvoiceDTO containing the details of the requested invoice.
     */
    @GetMapping("/invoice/{invoiceId}")
    public InvoiceDTO getInvoice(@PathVariable Long invoiceId) {
        log.info("GetMapping/getInvoice,invoiceId="+invoiceId);
        
        return billingService.getInvoice(invoiceId);
    }

    /**
     * Retrieves the list of invoices for a specific user.
     *
     * @param userId The ID of the user whose invoices we want to retrieve.
     * @return List of InvoiceDTO corresponding to the user's invoices.
     */
    @GetMapping("/user/{userId}")
    public List<InvoiceDTO> getInvoicesByUser(@PathVariable Long userId) {
        log.info("GetMapping/getInvoiceByUser,userId="+userId);
        
        return billingService.getInvoicesByUser(userId);
    }

    /**
     * Deletes an existing invoice based on its ID.
     *
     * @param invoiceId The ID of the invoice to delete.
     * @return A message indicating the deletion was successful.
     */
    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long invoiceId) {
        log.info("DeleteMapping/deleteInvoice,invoiceId="+invoiceId);
        
        billingService.deleteInvoice(invoiceId);
        return ResponseEntity.ok("Invoice deletion successful");        
    }
    
}
