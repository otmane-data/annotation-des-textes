package com.otmane.annotation_des_textes.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"annotateur","couples"})


public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date_limit", nullable = false)
    private Date dateLimite;

    @ManyToOne
    @JoinColumn(name = "annotateur_id")
    @JsonIgnoreProperties({"taches", "annotations", "password"})
    private Annotateur annotateur;

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    @JsonBackReference
    private Dataset dataset;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "tache_couple",
            joinColumns = @JoinColumn(name = "tache_id"),
            inverseJoinColumns = @JoinColumn(name = "couple_id")
    )
    @JsonIgnoreProperties("taches")
    private List<CoupleText> couples = new ArrayList<>();

}
