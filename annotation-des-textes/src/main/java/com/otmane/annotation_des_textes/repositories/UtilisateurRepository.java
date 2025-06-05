package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Role;
import com.otmane.annotation_des_textes.entities.RoleType;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByLogin(String login);
    Utilisateur findByNom(String nom);
    boolean existsByRole(Role role);
    List<Utilisateur> findByRole(RoleType role);
    boolean existsByLogin(String login);
}
