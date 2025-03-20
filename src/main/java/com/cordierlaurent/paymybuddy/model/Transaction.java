package com.cordierlaurent.paymybuddy.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
@Entity : indique que la classe correspond à une table de la base de données.
Indique que cette classe est une entité JPA (Java Persistence API).
Les attributs de la classe seront convertis en colonnes SQL ==>  si les noms sont similaires, les annotations @Table et @Column (avec name=) deviennent facultatives.
Non utilisation de @Data car génère equals() et hashCode(), ce qui peut être problématique pour les entités JPA (vu sur plusieurs sites) => à la place :
    @Getter + @Setter + @NoArgsConstructor + @AllArgsConstructor
*/
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// important pour afficher le contenu des objets simplement avec log4j2.
@ToString
public class Transaction {
    // Indique que id est la clé primaire.
    @Id
    // Indique que la valeur de la clé primaire est générée automatiquement par la base de données, en utilisant une auto-incrémentation.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    la base de données gère le timestamp => indiqué dans le schema :
    insertable = false => exclut la colonne lors d'une insertion.
    updatable = false => exclut cette colonne lors d'une mise à jour.
    */
    @Column(nullable = false, insertable = false, updatable = false)
    private Timestamp createdAt;
    
    /*
    @ManyToOne car plusieurs transactions peuvent appartenir à un utilisateur éméteur.
    @JoinColumn au lieu @Column pour indiquer à JPA comment faire la relation @ManyToOne et donc faire la jointure automatiquement.
    au lieu de :
        @Column(name = "sender_id", nullable = false)
        private Long senderId;
     */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /*
    @ManyToOne car plusieurs transactions peuvent appartenir à un utilisateur bénéficiare.
    @JoinColumn au lieu @Column pour indiquer à JPA comment faire la relation @ManyToOne et donc faire la jointure automatiquement.
    au lieu de :
        @Column(name = "receiver_id", nullable = false)
        private Long receiverId;
    */
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
}
