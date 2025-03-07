package com.cordierlaurent.paymybuddy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cordierlaurent.paymybuddy.dto.InvoiceDTO;
import com.cordierlaurent.paymybuddy.model.Transaction;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class TestBillingServiceImpl implements BillingService {

    @Autowired 
    TransactionService transactionService;
    
    private BigDecimal InvoiceTotalAmount (List<Transaction> transactions) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            log.info("transaction="+transaction);
            totalAmount = totalAmount.add(transaction.getAmount());
        }
        
        return totalAmount;
    }
    
    @Override
    public void createInvoice(Long userId, List<Transaction> transactions) {
        log.info("createInvoice,userId="+userId+",transactions="+transactions);
        
        log.info("transactions.size="+transactions.size()+",InvoiceTotalAmount="+InvoiceTotalAmount(transactions));
    }
    
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
    
    public @Override
    void deleteInvoice(Long invoiceId) {
        log.info("deleteInvoice,invoiceId="+invoiceId);
    }
    
}
