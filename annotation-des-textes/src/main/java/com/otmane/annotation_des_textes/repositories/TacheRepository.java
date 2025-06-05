package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {


    List<Tache> findByAnnotateur(Annotateur annotateur);

    List<Tache> findByDatasetIdAndAnnotateurId(Long datasetId, Long annotateurId);
    List<Tache> findByDataset(Dataset dataset);
    List<Tache> findByDateLimiteBefore(Date date);

    /**
     * Get all annotator IDs assigned to a specific dataset
     */
    @Query("SELECT DISTINCT t.annotateur.id FROM Tache t WHERE t.dataset.id = :datasetId AND t.annotateur IS NOT NULL")
    List<Long> findAnnotatorIdsByDatasetId(@Param("datasetId") Long datasetId);

    /**
     * Get the first deadline for a specific dataset
     */
    @Query("SELECT t.dateLimite FROM Tache t WHERE t.dataset.id = :datasetId AND t.dateLimite IS NOT NULL ORDER BY t.id ASC")
    List<Date> findDeadlinesByDatasetId(@Param("datasetId") Long datasetId);
}