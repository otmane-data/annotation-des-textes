package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Annotation;
import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByAnnotateur(Utilisateur utilisateur);
    Page<Annotation> findByAnnotateur(Utilisateur utilisateur, Pageable pageable);
    List<Annotation> findByCoupleText(CoupleText coupleText);
    Optional<Annotation> findByAnnotateurIdAndCoupleTextId(Long annotateurId, Long coupleId);

    List<Annotation> findByCoupleText_Dataset_Id(Long id);

    List<Annotation> findByAnnotateur_Taches_Id(Long taskId);

    List<Annotation> findByCoupleText_IdIn(List<Long> coupleIds);
    List<Annotation> findByAnnotateur_IdAndCoupleText_IdIn(Long annotateurId, List<Long> coupleIdsInTask);
}
