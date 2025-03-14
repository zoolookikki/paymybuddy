package com.cordierlaurent.paymybuddy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * For the form concerning the connection between users.
 */
//
// idem EmailDTO mais il est préférable d'avoir cette classe si il y a des évolutions + pour avoir la même structure/signification "...RequestDTO" qui veut dire DTO pour sécuriser les formulaires.
/*
@Data génère automatiquement des getters, setters, `toString`, equals, hashCode, et un constructeur par défaut => warning.
Par défaut, Lombok génère equals() et hashCode() uniquement pour les champs de ConnectionRequestDTO, et il ignore EmailDTO.
En ajoutant @EqualsAndHashCode(callSuper = true), il inclut les champs hérités de EmailDTO dans equals() et hashCode(), évitant ainsi des comparaisons incorrectes.
Idem pour toString(). 
*/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConnectionRequestDTO extends EmailDTO {
    public ConnectionRequestDTO(String email) {
        super(email);
    }
}
