package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoupleTextRepository extends JpaRepository<CoupleText, Long> {
    List<CoupleText> findByDataset(Dataset dataset);
    Page<CoupleText> findByDataset(Dataset dataset, Pageable pageable);
    long countByDatasetId(Long datasetId);

    // Alternative methods using JPQL
    @Query("SELECT ct FROM CoupleText ct WHERE ct.dataset.id = :datasetId")
    List<CoupleText> findByDatasetIdWithQuery(@Param("datasetId") Long datasetId);

    @Query("SELECT ct FROM CoupleText ct WHERE ct.dataset.id = :datasetId")
    Page<CoupleText> findByDatasetIdWithQuery(@Param("datasetId") Long datasetId, Pageable pageable);

    @Query("SELECT COUNT(ct) FROM CoupleText ct WHERE ct.dataset.id = :datasetId")
    long countByDatasetIdWithQuery(@Param("datasetId") Long datasetId);
}
