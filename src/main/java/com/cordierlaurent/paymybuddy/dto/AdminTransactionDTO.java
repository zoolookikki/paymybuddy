package com.cordierlaurent.paymybuddy.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Value;

/**
 * For displaying the list of transactions for the administrator.
 * <p>
 * @Value better than @Data for DTOs because it generates all fields final, meaning they cannot be changed after initialization.
 * To avoid problems with modifying DTOs due to passing by reference in argument and function return.
 * </p>
 */
@Value
public class AdminTransactionDTO {

    private Timestamp createdAt;
    private String userName;
    private String friendName;
    private String description;
    private BigDecimal amount;

    public AdminTransactionDTO(Timestamp createdAt, String userName, String friendName, String description, BigDecimal amount) {
        this.createdAt = createdAt;
        this.userName = userName;
        this.friendName = friendName;
        this.description = description;
        this.amount = amount;
    }
    
}
