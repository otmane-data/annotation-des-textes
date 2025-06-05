package com.otmane.annotation_des_textes.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "textClass")
@ToString(exclude = "dataset")
@NoArgsConstructor
@AllArgsConstructor
public class ClassPossible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String textClass;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;
}
