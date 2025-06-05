package com.otmane.annotation_des_textes.services;


import com.otmane.annotation_des_textes.entities.*;
import com.otmane.annotation_des_textes.repositories.AnnotationRepository;
import com.otmane.annotation_des_textes.repositories.TacheRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class AnnotationServiceImpl implements AnnotationService {
    private final AnnotationRepository annotationRepository;
    private final AnnotateurService annotateurService;
    private final CoupleTextService coupleTextService;
    private final TacheRepository tacheRepository;
    private final TacheService tacheService;

    public AnnotationServiceImpl(AnnotationRepository annotationRepository, AnnotateurService annotateurService, CoupleTextService coupleTextService, TacheRepository tacheRepository, TacheService tacheService) {
        this.annotationRepository = annotationRepository;
        this.annotateurService = annotateurService;
        this.coupleTextService = coupleTextService;
        this.tacheRepository = tacheRepository;
        this.tacheService = tacheService;
    }

    @Override
    public void saveAnnotation(String classSelectionText, Long coupleId, Long annotateurId) {
        Annotation annotation = annotationRepository.findByAnnotateurIdAndCoupleTextId(annotateurId, coupleId)
                .orElse(new Annotation());

        // Set the chosen class directly as the string value
        annotation.setChosenClass(classSelectionText);

        // Set the other fields if this is a new annotation
        if (annotation.getId() == null) {
            CoupleText coupleText = coupleTextService.findCoupleTextById(coupleId);
            Annotateur annotateur = annotateurService.findAnnotateurById(annotateurId);

            annotation.setCoupleText(coupleText);
            annotation.setAnnotateur(annotateur);
        }

        // Save the annotation
        annotationRepository.save(annotation);
    }
    
    @Override
    public long countTotalAnnotations() {
        return annotationRepository.count();
    }

    @Override
    public Integer countAnnotationsByDataset(Long id) {
        List<CoupleText> coupleTexts = coupleTextService.findAllCoupleTextsByDatasetId(id);
        int count = 0;
        for (CoupleText coupleText : coupleTexts) {
            count += annotationRepository.findByCoupleText(coupleText).size();
        }
        return count;
    }

    @Override
    public List<Annotation> findAllAnnotationsByUser(Utilisateur utilisateur) {
        if (utilisateur == null) {
            throw new IllegalArgumentException("Utilisateur cannot be null");
        }
        return annotationRepository.findByAnnotateur(utilisateur);
    }
    
    @Override
    public Page<Annotation> findAllAnnotationsByUser(Utilisateur utilisateur, Pageable pageable) {
        return annotationRepository.findByAnnotateur(utilisateur, pageable);
    }

    @Override
    public List<Annotation> findAllAnnotationsByDataset(Long id){
        return annotationRepository.findByCoupleText_Dataset_Id(id);
    }

    @Override
    public List<Annotation> findAnnotationsByTaskId(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Tache task = tacheService.findTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found with ID: " + taskId);
        }
        
        // Get all annotations for couples in this task
        List<Long> coupleIdsInTask = task.getCouples().stream()
                .map(CoupleText::getId)
                .collect(Collectors.toList());
                
        return annotationRepository.findByCoupleText_IdIn(coupleIdsInTask);
    }
    
    @Override
    public List<Annotation> findAnnotationsByAnnotatorForTask(Long taskId, Long annotateurId) {
        Tache task = tacheService.findTaskById(taskId); 
        List<Long> coupleIdsInTask = task.getCouples().stream()
                .map(CoupleText::getId)
                .collect(Collectors.toList());

        return annotationRepository.findByAnnotateur_IdAndCoupleText_IdIn(annotateurId, coupleIdsInTask);
    }
    
    @Override
    public Annotation findAnnotationById(Long id) {
        return annotationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annotation not found with ID: " + id));
    }
}
