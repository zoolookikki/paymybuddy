package com.cordierlaurent.paymybuddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base, abstract class for email, common to several DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class EmailDTO {
    
    @NotBlank(message = "L'e-mail est requis.")
    // @Email ne suffit pas...
    @Email(message = "Format d'e-mail invalide.")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "L'e-mail doit contenir un domaine valide (ex: .com, .fr)"
        )
    private String email;

}
