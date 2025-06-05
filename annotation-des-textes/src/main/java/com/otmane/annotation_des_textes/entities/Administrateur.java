package com.otmane.annotation_des_textes.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "administrateur")
@PrimaryKeyJoinColumn(name = "user-id")
@EqualsAndHashCode(callSuper = false)
public class Administrateur extends Utilisateur {

}