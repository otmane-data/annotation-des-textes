package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.services.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncDatasetParserService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDatasetParserService.class);

    @Autowired
    private DatasetService datasetService;

    @Async
    public void parseDatasetAsync(Dataset dataset) {
        try {
            logger.info("Starting async parsing for dataset: {} (ID: {})", dataset.getName(), dataset.getId());
            datasetService.ParseDataset(dataset);
            logger.info("Successfully completed async parsing for dataset: {} (ID: {})", dataset.getName(), dataset.getId());
        } catch (Exception e) {
            logger.error("Error during async parsing for dataset: {} (ID: {}): {}",
                        dataset.getName(), dataset.getId(), e.getMessage(), e);
            // Note: In a real application, you might want to update the dataset status
            // or send a notification about the parsing failure
        }
    }
}