package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Dataset;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface DatasetService {
    List<Dataset> findAllDatasets();
    Dataset findDatasetByName(String name);
    Dataset findDatasetById(Long id);
    void SaveDataset(Dataset dataset);
    Dataset createDataset(String name, String description, MultipartFile file, String classRaw) throws IOException;
    void ParseDataset(Dataset dataset);
    void deleteDataset(Long id);
    long countDatasets();

    List<Long> getAssignedAnnotatorIds(Long datasetId);

    Date getDatasetDeadline(Long datasetId);
}
