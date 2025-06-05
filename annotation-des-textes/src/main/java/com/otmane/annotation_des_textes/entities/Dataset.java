package com.otmane.annotation_des_textes.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"taches", "classesPossibles", "coupleTexts"})
@AllArgsConstructor
@NoArgsConstructor
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String filePath;
    private String fileType;

    @OneToMany(mappedBy = "dataset")
    private List<Tache> taches  = new ArrayList<>();

    @OneToMany(mappedBy = "dataset")
    private Set<CoupleText> coupleTexts  = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy="dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassPossible> classesPossibles = new HashSet<>();



}
