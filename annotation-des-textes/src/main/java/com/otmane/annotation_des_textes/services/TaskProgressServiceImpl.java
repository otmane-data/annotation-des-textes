package com.otmane.annotation_des_textes.services;
import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.ProgressTache;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import com.otmane.annotation_des_textes.repositories.AnnotationRepository;
import com.otmane.annotation_des_textes.repositories.ProgressTacheRepository;
import com.otmane.annotation_des_textes.repositories.TacheRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskProgressServiceImpl implements TaskProgressService {

    private final ProgressTacheRepository progressTacheRepository;
    private final TacheRepository tacheRepository;
    private final AnnotationRepository annotationRepository;

    @Autowired
    public TaskProgressServiceImpl(
            ProgressTacheRepository progressTacheRepository,
            TacheRepository tacheRepository,
            AnnotationRepository annotationRepository) {
        this.progressTacheRepository = progressTacheRepository;
        this.tacheRepository = tacheRepository;
        this.annotationRepository = annotationRepository;
    }

    @Override
    public Optional<ProgressTache> getProgressForUserAndTask(Utilisateur utilisateur, Long taskId) {
        Tache tache = tacheRepository.findById(taskId).orElse(null);
        return progressTacheRepository.findByUtilisateurAndTache(utilisateur, tache);
    }

    @Override
    @Transactional
    public void saveOrUpdateProgress(Utilisateur utilisateur, Long taskId, int index) {
        Tache tache = tacheRepository.findById(taskId).orElse(null);
        if (tache == null) {
            throw new IllegalArgumentException("Tâche introuvable avec l'ID: " + taskId);
        }
        
        ProgressTache progress = progressTacheRepository.findByUtilisateurAndTache(utilisateur, tache)
                .orElse(new ProgressTache(utilisateur, tache));

        progress.setDernierIndex(index);
        progress.setDerniereMiseAJour(LocalDateTime.now());
        progressTacheRepository.save(progress);
    }
    
    @Override
    public ProgressTache getLastAnnotationByUser(Utilisateur utilisateur) {
        List<ProgressTache> progressList = progressTacheRepository.findTopByUtilisateurAfterOrderByUpdatedAtDesc(utilisateur);
        return progressList.isEmpty() ? null : progressList.get(0);
    }
    
    @Override
    public ProgressTache getLastAnnotationByUser(Annotateur annotateur) {
        // Considère l'annotateur comme un utilisateur puisque Annotateur hérite de Utilisateur
        return getLastAnnotationByUser((Utilisateur) annotateur);
    }
    
    /**
     * Calcule le pourcentage de progression pour une tâche
     * @param tache la tâche
     * @param annotateur l'annotateur
     * @return le pourcentage de progression
     */
    @Override
    public int calculateProgressPercentage(Tache tache, Annotateur annotateur) {
        if (tache == null || tache.getCouples() == null || tache.getCouples().isEmpty()) {
            return 0;
        }
        
        // Extraire les IDs des couples de textes pour cette tâche
        List<Long> coupleIds = tache.getCouples().stream()
                .map(CoupleText::getId)
                .collect(Collectors.toList());
        
        // Si aucun couple, retourner 0%
        if (coupleIds.isEmpty()) {
            return 0;
        }
        
        // Récupérer les annotations de cet annotateur pour ces couples
        List<Long> annotatedCoupleIds = annotationRepository
                .findByAnnotateur_IdAndCoupleText_IdIn(annotateur.getId(), coupleIds)
                .stream()
                .map(a -> a.getCoupleText().getId())
                .collect(Collectors.toList());
        
        // Calculer le pourcentage
        long totalCouples = coupleIds.size();
        long annotatedCouples = annotatedCoupleIds.size();
        
        return (int) ((annotatedCouples * 100) / totalCouples);
    }
    
    @Override
    @Transactional
    public double calculateOverallTaskCompletionPercentage() {
        try {
            // Récupérer toutes les tâches avec leurs relations
            List<Tache> allTasks = tacheRepository.findAll();
            
            if (allTasks.isEmpty()) {
                return 0.0;
            }
            
            long totalCouples = 0;
            long totalAnnotatedCouples = 0;
            
            for (Tache tache : allTasks) {
                if (tache.getCouples() == null || tache.getCouples().isEmpty() || tache.getAnnotateur() == null) {
                    continue;
                }
                
                List<Long> coupleIds = tache.getCouples().stream()
                        .map(CoupleText::getId)
                        .collect(Collectors.toList());
                
                if (!coupleIds.isEmpty()) {
                    totalCouples += coupleIds.size();
                    
                    // Optimisation: Récupérer le compte des annotations directement
                    long annotatedCount = annotationRepository
                            .findByAnnotateur_IdAndCoupleText_IdIn(tache.getAnnotateur().getId(), coupleIds)
                            .size();
                    
                    totalAnnotatedCouples += annotatedCount;
                }
            }
            
            if (totalCouples == 0) {
                return 0.0;
            }
            
            return Math.round((totalAnnotatedCouples * 100.0) / totalCouples * 10) / 10.0;
            
        } catch (Exception e) {
            throw new RuntimeException("Error calculating overall task completion: " + e.getMessage(), e);
        }
    }
}
