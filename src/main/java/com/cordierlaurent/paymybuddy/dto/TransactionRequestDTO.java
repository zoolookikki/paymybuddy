package com.cordierlaurent.paymybuddy.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * For transaction entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    
    // tous les @NotNull ou @NotBlank cas impossibles car required sur les formulaires mais par sécurité.
    @NotNull(message = "Le destinataire est requis")
    private Long receiverId;

    @NotBlank(message = "La description est requise")
    private String description;

    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;
}
