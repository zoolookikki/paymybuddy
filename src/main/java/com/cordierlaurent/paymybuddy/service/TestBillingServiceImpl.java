package com.cordierlaurent.paymybuddy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.dto.InvoiceDTO;
import com.cordierlaurent.paymybuddy.model.Transaction;

import lombok.extern.log4j.Log4j2;

/**
 * Fictitious implementation of the billing service for testing and simulations.
 * <p>
 * This class provides methods for creating, retrieving, and deleting invoices associated with a user's transactions.
 * </p>
 */
@Service
@Log4j2
public class TestBillingServiceImpl implements BillingService {

    @Autowired 
    TransactionService transactionService;
    
    /**
     * Calculates the total amount of a list of transactions.
     *
     * @param transactions List of transactions to include in the calculation.
     * @return The total transaction amount as a BigDecimal.
     */
    private BigDecimal InvoiceTotalAmount (List<Transaction> transactions) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            log.info("transaction="+transaction);
            totalAmount = totalAmount.add(transaction.getAmount());
        }
        
        return totalAmount;
    }
    
    /**
     * Creates an invoice for a given user based on their transactions.
     * <p>
     * This method is a simulation that displays invoice information in the logs without persisting it to the database.
     * </p>
     *
     * @param userId The ID of the user concerned.
     * @param transactions The list of transactions associated with the invoice.
     */
    @Override
    public void createInvoice(Long userId, List<Transaction> transactions) {
        log.info("createInvoice,userId="+userId+",transactions="+transactions);
        
        log.info("transactions.size="+transactions.size()+",InvoiceTotalAmount="+InvoiceTotalAmount(transactions));
    }
    

    /**
     * Retrieves a specific invoice.
     * <p>
     * This method is a simulation that returns a dummy invoice associated with a dummy user (ID = 1) and their transactions.
     * </p>
     *
     * @param invoiceId The ID of the invoice to be retrieved.
     * @return An InvoiceDTO object representing the simulated invoice.
     */
    @Override
    public InvoiceDTO getInvoice(Long invoiceId) {
        log.info("getInvoice,invoiceId="+invoiceId);

        // SIMULATION SUR userId = 1;
        List<Transaction> transactions = transactionService.getUserTransactions(1L);

        return new InvoiceDTO(
                invoiceId,
                1L, 
                transactions,
                InvoiceTotalAmount(transactions));
    }
    

    /**
     * Retrieves the list of invoices for a given user.
     * <p>
     * This method simulates the generation of two invoices with the same content but different numbers.
     * </p>
     *
     * @param userId The user ID of the user whose invoices we want to retrieve.
     * @return A list of InvoiceDTOs representing the simulated invoices.
     */
    @Override
    public List<InvoiceDTO> getInvoicesByUser(Long userId) {
        log.info("getInvoicesByUser,userId="+userId);
        
        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        // SIMULATION 2 fois la même facture avec des n° différents.
        return List.of(
                new InvoiceDTO(1L,
                        userId, 
                        transactions,
                        InvoiceTotalAmount(transactions)),
                new InvoiceDTO(2L,
                        userId, 
                        transactions,
                        InvoiceTotalAmount(transactions))
                );
    }
    

    /**
     * Deletes a specific invoice.
     * <p>
     * This method is a simulation that simply displays a message in the logs without actually deleting anything from the database.
     * </p>
     *
     * @param invoiceId The ID of the invoice to be deleted.
     */
    @Override
    public void deleteInvoice(Long invoiceId) {
        log.info("deleteInvoice,invoiceId="+invoiceId);
    }
    
}
