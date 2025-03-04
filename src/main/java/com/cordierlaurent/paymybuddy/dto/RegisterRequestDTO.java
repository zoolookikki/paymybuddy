package com.cordierlaurent.paymybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

//pour la saisie du formulaire d'inscription.
/*
@Data génère automatiquement des getters, setters, `toString`, equals, hashCode, et un constructeur par défaut => warning.
Par défaut, Lombok génère equals() et hashCode() uniquement pour les champs de RegisterRequestDTO, et il ignore CommonUserDTO.
En ajoutant @EqualsAndHashCode(callSuper = true), il inclut les champs hérités de CommonUserDTO dans equals() et hashCode(), évitant ainsi des comparaisons incorrectes.
Idem pour toString(). 
*/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RegisterRequestDTO extends CommonUserDTO {

    // tous les @NotBlank cas impossibles car required sur le formulaire d'inscription mais par sécurité.
    @NotBlank(message = "Le mot de passe est requis.")
    /*
    (?=.*[A-Z]) : au moins une majuscule.
    (?=.*\\d) : au moins un chiffre.
    (?=.*[@#$%^&+=!]) : au moins un caractère spécial.
    .{8,}$ : minimum 8 caractères.
    */
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial."
            )    
    private String password;
    
    public RegisterRequestDTO(String name, String email, String password) {
        super(name, email);
        this.password = password;
    }    

}
