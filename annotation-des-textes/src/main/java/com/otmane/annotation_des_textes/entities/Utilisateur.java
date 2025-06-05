package com.otmane.annotation_des_textes.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode(of = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Utilisateur {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false)
   private String nom;

   @Column(nullable = false)
   private String prenom;

   @Column(nullable = false)
   private String login;
   @Column(nullable = false)
   private  String password;

   @Column(nullable = false, columnDefinition = "boolean default false")
   private boolean deleted;

   @ManyToOne
   @JoinColumn(name = "role_id", nullable = false)
   private Role role;

   @Column
   private String email;

   public String getEmail() {
       return email;
   }

   public void setEmail(String email) {
       this.email = email;
   }
}