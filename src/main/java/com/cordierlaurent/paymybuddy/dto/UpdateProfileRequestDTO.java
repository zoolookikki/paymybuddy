package com.cordierlaurent.paymybuddy.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * For modification of the profile form.
 */
/*
@Data génère automatiquement des getters, setters, `toString`, equals, hashCode, et un constructeur par défaut => warning.
Par défaut, Lombok génère equals() et hashCode() uniquement pour les champs de UpdateProfileRequestDTO, et il ignore CommonUserDTO.
En ajoutant @EqualsAndHashCode(callSuper = true), il inclut les champs hérités de CommonUserDTO dans equals() et hashCode(), évitant ainsi des comparaisons incorrectes.
Idem pour toString(). 
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UpdateProfileRequestDTO extends CommonUserDTO {

    // Attention, ici, le mot de passe n'est pas obligatoire car on le modifie que si il est saisi => ^$| (vide ou ...) en plus (voir RegisterRequestDTO pour commentaires sur le reste).
    @Pattern(
            regexp = "^$|^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial."
        )
    private String password;
    
    public UpdateProfileRequestDTO(String name, String email, String password) {
        super(name, email);
        this.password = password;
    }    
    
}
