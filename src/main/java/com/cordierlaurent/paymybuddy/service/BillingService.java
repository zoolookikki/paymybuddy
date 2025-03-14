package com.cordierlaurent.paymybuddy.service;

import java.util.List;

import com.cordierlaurent.paymybuddy.dto.InvoiceDTO;
import com.cordierlaurent.paymybuddy.model.Transaction;

/**
 * Billing service for managing user invoices.
 * <p>
 * This interface defines the operations for creating, retrieving, and deleting invoices associated with user transactions.
 * </p>
 */
public interface BillingService {
    
    /**
     * Creates an invoice for a given user based on their transactions.
     *
     * @param userId The ID of the user for whom the invoice is created.
     * @param transactions The list of transactions associated with this invoice.
     */
    void createInvoice(Long userId, List<Transaction> transactions);
    
    /**
     * Retrieves a specific invoice by its ID.
     *
     * @param invoiceId The ID of the invoice to be retrieved.
     * @return An InvoiceDTO object representing the requested invoice.
     */
    InvoiceDTO getInvoice(Long invoiceId);
    
    /**
     * Retrieves all invoices associated with a given user.
     *
     * @param userId The user ID of the user whose invoices we wish to retrieve.
     * @return A list of InvoiceDTO objects corresponding to the user's invoices.
     */
    List<InvoiceDTO> getInvoicesByUser(Long userId);
    
    /**
     * Deletes a specific invoice from the database.
     *
     * @param invoiceId The ID of the invoice to be deleted.
     */
    void deleteInvoice(Long invoiceId);
    
}
