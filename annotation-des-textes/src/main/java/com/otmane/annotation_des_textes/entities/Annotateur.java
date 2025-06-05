package com.otmane.annotation_des_textes.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "annotateurs")
@EqualsAndHashCode(callSuper = false)
public class Annotateur extends Utilisateur {

    @OneToMany(mappedBy = "annotateur",cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"annotateur","couples"})
    private List<Tache> taches = new ArrayList<>();

    @OneToMany(mappedBy = "annotateur",cascade = CascadeType.ALL)
    private List<Annotation> annotation = new ArrayList<>();
    
    /**
     * Méthode pour compatibilité avec l'ancien code
     */
    public List<Annotation> getAnnotations() {
        return this.annotation;
    }
    @OneToMany(mappedBy="annotateur", cascade = CascadeType.ALL)
    private List<SpamDetectionResults> spamDetectionResults = new ArrayList<>();

}