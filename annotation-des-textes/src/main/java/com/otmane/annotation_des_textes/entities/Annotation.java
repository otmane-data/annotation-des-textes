package com.otmane.annotation_des_textes.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"annotateur", "coupleText"})
public class Annotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,name = "classe_choisie")
    private String classeChoisie;

    @ManyToOne
    @JoinColumn(name = "annotateur_id")
    private Annotateur annotateur;

    @ManyToOne
    @JoinColumn(name = "couple_id")
    private CoupleText coupleText;

    public String getChosenClass() {
        return classeChoisie;
    }

    public void setChosenClass(String classSelectionText) {
        this.classeChoisie = classSelectionText;
    }
}