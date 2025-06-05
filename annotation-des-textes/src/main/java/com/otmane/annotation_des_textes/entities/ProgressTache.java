package com.otmane.annotation_des_textes.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"utilisateur", "tache"})
public class ProgressTache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tache_id")
    private Tache tache;

    @Column(name = "dernier_index")
    @Min(value = 0, message = "L'index ne peut pas être négatif")
    private int dernierIndex;

    @Column(name = "derniere_mise_a_jour", nullable = false)
    @NotNull(message = "La date de mise à jour ne peut pas être nulle")
    private LocalDateTime derniereMiseAJour;

    // Constructeur avec deux paramètres
    public ProgressTache(Utilisateur utilisateur, Tache tache) {
        this.utilisateur = utilisateur;
        this.tache = tache;
        this.dernierIndex = 0;
        this.derniereMiseAJour = LocalDateTime.now();
    }
}