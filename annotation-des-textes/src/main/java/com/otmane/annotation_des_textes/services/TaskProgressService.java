package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.ProgressTache;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import com.otmane.annotation_des_textes.repositories.AnnotationRepository;
import com.otmane.annotation_des_textes.repositories.ProgressTacheRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Interface pour les services de gestion de la progression des tâches
 */
public interface TaskProgressService {
    
    /**
     * Récupère la progression pour un utilisateur et une tâche
     * @param utilisateur l'utilisateur
     * @param taskId l'identifiant de la tâche
     * @return la progression si elle existe
     */
    Optional<ProgressTache> getProgressForUserAndTask(Utilisateur utilisateur, Long taskId);
    
    /**
     * Sauvegarde ou met à jour la progression pour un utilisateur et une tâche
     * @param utilisateur l'utilisateur
     * @param taskId l'identifiant de la tâche
     * @param index l'index actuel
     */
    void saveOrUpdateProgress(Utilisateur utilisateur, Long taskId, int index);
    
    /**
     * Récupère la dernière annotation d'un utilisateur
     * @param utilisateur l'utilisateur
     * @return la dernière progression
     */
    ProgressTache getLastAnnotationByUser(Utilisateur utilisateur);
    
    /**
     * Récupère la dernière annotation d'un annotateur
     * @param annotateur l'annotateur
     * @return la dernière progression
     */
    ProgressTache getLastAnnotationByUser(Annotateur annotateur);
    
    /**
     * Calcule le pourcentage de progression pour une tâche
     * @param tache la tâche
     * @param annotateur l'annotateur
     * @return le pourcentage de progression
     */
    int calculateProgressPercentage(Tache tache, Annotateur annotateur);
    
    /**
     * Calcule le pourcentage global de complétion des tâches dans le système
     * @return le pourcentage global de complétion
     */
    double calculateOverallTaskCompletionPercentage();
}