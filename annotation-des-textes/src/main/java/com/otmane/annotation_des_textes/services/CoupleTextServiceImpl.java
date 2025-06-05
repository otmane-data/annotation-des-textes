package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.repositories.CoupleTextRepository;
import com.otmane.annotation_des_textes.repositories.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CoupleTextServiceImpl implements CoupleTextService {

    private final CoupleTextRepository coupleTextRepository;
    private final DatasetRepository datasetRepository;

    @Autowired
    public CoupleTextServiceImpl(CoupleTextRepository coupleTextRepository,
                                 DatasetRepository datasetRepository) {
        this.coupleTextRepository = coupleTextRepository;
        this.datasetRepository = datasetRepository;
    }

    @Override
    public CoupleText findCoupleTextById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("CoupleText ID cannot be null");
        }
        return coupleTextRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CoupleText not found with ID: " + id));
    }

    @Override
    public Page<CoupleText> getCoupleTexts(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return coupleTextRepository.findAll(pageable);
    }

    @Override
    public List<CoupleText> findAllCoupleTextsByDatasetId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Dataset ID cannot be null");
        }
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset not found with ID: " + id));
        return coupleTextRepository.findByDataset(dataset);
    }

    @Override
    public long countCoupleTextsByDatasetId(Long datasetId) {
        long count = coupleTextRepository.countByDatasetIdWithQuery(datasetId);
        System.out.println("DEBUG: countCoupleTextsByDatasetId for dataset " + datasetId + " = " + count);

        // Also try the original method for comparison
        long originalCount = coupleTextRepository.countByDatasetId(datasetId);
        System.out.println("DEBUG: Original count method result = " + originalCount);

        return count;
    }

    @Override
    public Page<CoupleText> getCoupleTextsByDatasetId(Long datasetId, int page, int size) {
        System.out.println("DEBUG: getCoupleTextsByDatasetId called with datasetId=" + datasetId + ", page=" + page + ", size=" + size);

        // Try direct query first
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<CoupleText> result = coupleTextRepository.findByDatasetIdWithQuery(datasetId, pageable);

        System.out.println("DEBUG: Direct query found " + result.getTotalElements() + " couples for dataset " + datasetId);
        System.out.println("DEBUG: Page content size: " + result.getContent().size());

        // Debug: Print first few couples if any
        if (!result.getContent().isEmpty()) {
            System.out.println("DEBUG: First couple - Text1: " + result.getContent().get(0).getTexte1().substring(0, Math.min(50, result.getContent().get(0).getTexte1().length())));
        } else {
            System.out.println("DEBUG: No couples found, trying alternative method...");

            // Fallback to original method
            Optional<Dataset> datasetOptional = datasetRepository.findById(datasetId);
            if (datasetOptional.isPresent()) {
                Dataset dataset = datasetOptional.get();
                System.out.println("DEBUG: Dataset found: " + dataset.getName() + " (id=" + dataset.getId() + ")");
                result = coupleTextRepository.findByDataset(dataset, pageable);
                System.out.println("DEBUG: Alternative method found " + result.getTotalElements() + " couples");
            } else {
                System.out.println("DEBUG: Dataset not found with id: " + datasetId);
            }
        }

        return result;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatch(List<CoupleText> batch) {
        if (batch == null || batch.isEmpty()) {
            throw new IllegalArgumentException("CoupleTexts list cannot be null or empty");
        }

        System.out.println("DEBUG: Saving batch of " + batch.size() + " couples");

        // Ensure all couples have proper dataset reference
        for (CoupleText couple : batch) {
            if (couple.getDataset() == null) {
                throw new IllegalStateException("CoupleText must have a dataset reference");
            }
            if (couple.getTexte1() == null || couple.getTexte2() == null) {
                throw new IllegalStateException("CoupleText must have both text1 and text2");
            }
        }

        List<CoupleText> savedCouples = coupleTextRepository.saveAll(batch);
        coupleTextRepository.flush();

        System.out.println("DEBUG: Successfully saved " + savedCouples.size() + " couples");

        // Verify the save was successful
        if (!savedCouples.isEmpty()) {
            Long datasetId = savedCouples.get(0).getDataset().getId();
            long totalCount = coupleTextRepository.countByDatasetIdWithQuery(datasetId);
            System.out.println("DEBUG: Total couples in dataset " + datasetId + " after save: " + totalCount);
        }
    }

}