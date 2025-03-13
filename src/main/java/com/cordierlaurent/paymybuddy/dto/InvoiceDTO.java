package com.cordierlaurent.paymybuddy.dto;

import java.math.BigDecimal;
import java.util.List;

import com.cordierlaurent.paymybuddy.model.Transaction;

import lombok.Value;

//pour l'affichage de la liste des transactions pour les utilisateurs.
//@Value better than @Data for DTOs because it generates all fields final, meaning they cannot be changed after initialization.
//To avoid problems with modifying DTOs due to passing by reference in argument and function return.
@Value
public class InvoiceDTO {
    private Long invoiceId;
    private Long userId;
    private List<Transaction> transactions;
    private BigDecimal amount;
    
    public InvoiceDTO(Long invoiceId, Long userId, List<Transaction> transactions, BigDecimal amount) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.transactions = transactions;
        this.amount = amount;
    }
    
}
