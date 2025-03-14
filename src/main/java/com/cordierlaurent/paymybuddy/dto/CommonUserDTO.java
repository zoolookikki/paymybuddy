package com.cordierlaurent.paymybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Base, abstract, user-related class common to multiple DTOs.
 * <p>
 * @Value better than @Data for DTOs because it generates all fields final, meaning they cannot be changed after initialization.
 * To avoid problems with modifying DTOs due to passing by reference in argument and function return.
 * </p>
 */
/*
@Data génère automatiquement des getters, setters, `toString`, equals, hashCode, et un constructeur par défaut => warning.
Par défaut, Lombok génère equals() et hashCode() uniquement pour les champs de CommonUserDTO, et il ignore EmailDTO.
En ajoutant @EqualsAndHashCode(callSuper = true), il inclut les champs hérités de EmailDTO dans equals() et hashCode(), évitant ainsi des comparaisons incorrectes.
Idem pour toString(). 
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class CommonUserDTO extends EmailDTO {
    
    // tous les @NotBlank cas impossibles car required sur les formulaires mais par sécurité.
    @NotBlank(message = "Le nom est requis.")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9 -]+$", 
            message = "Le nom ne doit contenir que des lettres, chiffres, espaces ou tirets."
            )
    
    private String name;

    public CommonUserDTO(String name, String email) {
        super(email);
        this.name = name;
    }
    
}
