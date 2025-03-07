package com.cordierlaurent.paymybuddy.service;

import java.util.List;

import com.cordierlaurent.paymybuddy.dto.InvoiceDTO;
import com.cordierlaurent.paymybuddy.model.Transaction;

public interface BillingService {
    
    void createInvoice(Long userId, List<Transaction> transactions);
    InvoiceDTO getInvoice(Long invoiceId);
    List<InvoiceDTO> getInvoicesByUser(Long userId);
    void deleteInvoice(Long invoiceId);
    
}    

