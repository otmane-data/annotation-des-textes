package com.otmane.annotation_des_textes.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"texte1", "texte2", "dataset"})
@ToString(exclude = {"taches", "annotations"})
public class CoupleText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_1", columnDefinition = "LONGTEXT", nullable = false)
    private String texte1;

    @Column(name = "text_2", columnDefinition = "LONGTEXT", nullable = false)
    private String texte2;

    @Column(name = "original_id")
    private Long originalId; // Pour suivre l'ID original si dupliqué

    // Plusieurs tâches peuvent réutiliser ce couple
    @ManyToMany(mappedBy = "couples")
    @JsonIgnoreProperties({"couples", "annotateurs", "classesPossibles", "dataset"})
    private List<Tache> taches = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "dataset_id", nullable = false)
    @JsonBackReference
    private Dataset dataset;

    @OneToMany(mappedBy = "coupleText", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Annotation> annotations = new ArrayList<>();

    /**
     * Constructeur de copie — utile pour dupliquer un couple tout en conservant la référence d'origine.
     */
    public CoupleText(CoupleText original) {
        this.texte1 = original.getTexte1();
        this.texte2 = original.getTexte2();
        this.dataset = original.getDataset();
        this.originalId = original.getId();
    }
    
    /**
     * Méthode pour compatibilité avec l'ancien code
     */
    public String getText_1() {
        return this.texte1;
    }
    
    /**
     * Méthode pour compatibilité avec l'ancien code
     */
    public String getText_2() {
        return this.texte2;
    }
}
