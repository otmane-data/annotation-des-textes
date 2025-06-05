package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.ProgressTache;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressTacheRepository extends JpaRepository<ProgressTache, Long> {
    Optional<ProgressTache> findByUtilisateurAndTache(Utilisateur user, Tache task);
    
    @Query("SELECT tp FROM ProgressTache tp WHERE tp.utilisateur = ?1 ORDER BY tp.derniereMiseAJour DESC")
    List<ProgressTache> findTopByUtilisateurAfterOrderByUpdatedAtDesc(Utilisateur user);
}