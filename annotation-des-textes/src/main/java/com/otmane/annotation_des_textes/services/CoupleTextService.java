package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.CoupleText;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for managing text couples in the annotation system
 */
public interface CoupleTextService {
    /**
     * Find a couple text by its ID
     * @param id The ID of the couple text to find
     * @return The couple text if found
     * @throws IllegalArgumentException if couple text not found
     */
    CoupleText findCoupleTextById(Long id);

    /**
     * Get paginated couple texts
     * @param page Page number
     * @param size Page size
     * @return Page of couple texts
     */
    Page<CoupleText> getCoupleTexts(int page, int size);

    /**
     * Get paginated couple texts by dataset ID
     * @param datasetId The ID of the dataset
     * @param page Page number
     * @param size Page size
     * @return Page of couple texts
     */
    Page<CoupleText> getCoupleTextsByDatasetId(Long datasetId, int page, int size);

    /**
     * Find all couple texts belonging to a specific dataset
     * @param id The ID of the dataset
     * @return List of couple texts in the dataset
     */
    List<CoupleText> findAllCoupleTextsByDatasetId(Long id);

    /**
     * Count couple texts in a dataset
     * @param id The ID of the dataset
     * @return Number of couple texts in the dataset
     */
    long countCoupleTextsByDatasetId(Long id);

    /**
     * Save a batch of couple texts
     * @param batch List of couple texts to save
     */
    void saveBatch(List<CoupleText> batch);
}
