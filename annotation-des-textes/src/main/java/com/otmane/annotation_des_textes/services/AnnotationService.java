package com.otmane.annotation_des_textes.services;
import com.otmane.annotation_des_textes.entities.Annotation;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnnotationService {
    Annotation findAnnotationById(Long id);
    void saveAnnotation(String text, Long coupleId, Long annotateurId);
    long countTotalAnnotations();
    Integer countAnnotationsByDataset(Long id);
    List<Annotation> findAllAnnotationsByUser(Utilisateur user);
    Page<Annotation> findAllAnnotationsByUser(Utilisateur utilisateur, Pageable pageable);
    List<Annotation> findAllAnnotationsByDataset(Long id);
    List<Annotation> findAnnotationsByAnnotatorForTask(Long taskId, Long annotateurId);
    /**
     * Find all annotations for text couples in a specific task
     */
    List<Annotation> findAnnotationsByTaskId(Long taskId);
}
