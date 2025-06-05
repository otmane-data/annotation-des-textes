package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.SpamDetectionResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpamDetectionResultsRepository extends JpaRepository<SpamDetectionResults, Long> {
    List<SpamDetectionResults> findByAnnotateur(Annotateur annotateur);
    List<SpamDetectionResults> findByFlagged(boolean flagged);
    List<SpamDetectionResults> findByAnnotateurAndFlagged(Annotateur annotateur, boolean flagged);
    List<SpamDetectionResults> findByAnnotateurId(Long annotateurId);
} 