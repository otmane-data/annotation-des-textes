package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.ClassPossible;
import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.repositories.ClassPossibleRepository;
import com.otmane.annotation_des_textes.repositories.CoupleTextRepository;
import com.otmane.annotation_des_textes.repositories.DatasetRepository;
import com.otmane.annotation_des_textes.repositories.TacheRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class DatasetServiceImpl implements DatasetService {

    private static final Logger logger = LoggerFactory.getLogger(DatasetServiceImpl.class);
    private static final String UPLOAD_DIR = "uploads/datasets";

    private final DatasetRepository datasetRepository;
    private final ClassPossibleRepository classPossibleRepository;

    @Autowired
    private CoupleTextService coupleTextService;
    @Autowired
    private CoupleTextRepository coupleTextRepository;
    @Autowired
    private TacheRepository tacheRepository;

    public DatasetServiceImpl(DatasetRepository datasetRepository, ClassPossibleRepository classPossibleRepository) {
        this.datasetRepository = datasetRepository;
        this.classPossibleRepository = classPossibleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dataset> findAllDatasets() {
        List<Dataset> datasets = datasetRepository.findAll();
        datasets.forEach(dataset -> {
            dataset.getClassesPossibles().size(); // Force loading of classesPossibles
            if (dataset.getCoupleTexts() != null) {
                dataset.getCoupleTexts().size(); // Force loading of coupleTexts
            }
        });
        return datasets;
    }

    @Override
    @Transactional(readOnly = true)
    public Dataset findDatasetByName(String name) {
        Dataset dataset = datasetRepository.findByName(name);
        if (dataset != null) {
            dataset.getClassesPossibles().size(); // Force loading of classesPossibles
            if (dataset.getCoupleTexts() != null) {
                dataset.getCoupleTexts().size(); // Force loading of coupleTexts
            }
        }
        return dataset;
    }

    @Override
    @Transactional(readOnly = true)
    public Dataset findDatasetById(Long id) {
        Dataset dataset = datasetRepository.findById(id).orElse(null);
        if (dataset != null) {
            dataset.getClassesPossibles().size(); // Force loading of classesPossibles
            if (dataset.getCoupleTexts() != null) {
                dataset.getCoupleTexts().size(); // Force loading of coupleTexts
            }
            // Verify file existence
            String filePath = dataset.getFilePath();
            if (filePath != null && !Files.exists(Paths.get(filePath))) {
                dataset.setFilePath(null); // Clear invalid file path
            }
        }
        return dataset;
    }

    @Override
    public void ParseDataset(Dataset dataset) {
        final int MAX_ROWS = 1000;
        final int BATCH_SIZE = 25;

        String filename = dataset.getFilePath();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Dataset has no associated file");
        }

        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            File resourceFile = new File(filename);
            if (resourceFile.exists()) {
                filePath = resourceFile.toPath();
            } else {
                throw new RuntimeException("File not found: " + filePath.toString());
            }
        }

        logger.info("Starting to parse dataset file: {}", filePath);

        // Determine file type by extension
        String fileName = filePath.getFileName().toString().toLowerCase();

        try {
            List<CoupleText> batch = new ArrayList<>();
            int rowCount = 0;

            if (fileName.endsWith(".csv")) {
                logger.info("Parsing CSV file...");
                rowCount = parseCsvFile(filePath, dataset, batch, MAX_ROWS, BATCH_SIZE);
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                logger.info("Parsing Excel file...");
                rowCount = parseExcelFile(filePath, dataset, batch, MAX_ROWS, BATCH_SIZE);
            } else {
                throw new RuntimeException("Unsupported file format. Only CSV and Excel files are supported.");
            }

            // Save any remaining couples in the batch
            if (!batch.isEmpty()) {
                coupleTextService.saveBatch(batch);
                logger.info("Saved final batch of {} couples", batch.size());
            }

            logger.info("Successfully parsed {} rows from dataset file", rowCount);

        } catch (Exception e) {
            logger.error("Error parsing dataset file: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }

    private int parseCsvFile(Path filePath, Dataset dataset, List<CoupleText> batch, int maxRows, int batchSize) throws IOException {
        int rowCount = 0;

        // Ensure dataset is managed by the persistence context
        Dataset managedDataset = datasetRepository.findById(dataset.getId()).orElse(dataset);
        logger.info("Using managed dataset: {} (ID: {})", managedDataset.getName(), managedDataset.getId());

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null && rowCount < maxRows) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Parse CSV line (handle quoted values)
                String[] parts = parseCsvLine(line);

                if (parts.length >= 2) {
                    String text1 = parts[0].trim();
                    String text2 = parts[1].trim();

                    if (!text1.isEmpty() && !text2.isEmpty()) {
                        CoupleText couple = new CoupleText();
                        couple.setTexte1(text1);
                        couple.setTexte2(text2);
                        couple.setDataset(managedDataset);

                        batch.add(couple);
                        rowCount++;

                        logger.debug("Added couple {}: '{}' - '{}'", rowCount,
                                   text1.length() > 50 ? text1.substring(0, 50) + "..." : text1,
                                   text2.length() > 50 ? text2.substring(0, 50) + "..." : text2);

                        if (batch.size() == batchSize) {
                            coupleTextService.saveBatch(batch);
                            logger.info("Saved batch of {} couples (total processed: {})", batch.size(), rowCount);
                            batch.clear();
                        }
                    }
                }
            }
        }

        return rowCount;
    }

    private int parseExcelFile(Path filePath, Dataset dataset, List<CoupleText> batch, int maxRows, int batchSize) throws IOException {
        int rowCount = 0;

        // Ensure dataset is managed by the persistence context
        Dataset managedDataset = datasetRepository.findById(dataset.getId()).orElse(dataset);
        logger.info("Using managed dataset: {} (ID: {})", managedDataset.getName(), managedDataset.getId());

        try (InputStream fileInputStream = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext() && rowCount < maxRows) {
                Row row = rowIterator.next();
                Cell text1Cell = row.getCell(0);
                Cell text2Cell = row.getCell(1);

                if (text1Cell == null || text2Cell == null) continue;

                String text1 = getCellValueAsString(text1Cell).trim();
                String text2 = getCellValueAsString(text2Cell).trim();

                if (!text1.isEmpty() && !text2.isEmpty()) {
                    CoupleText couple = new CoupleText();
                    couple.setTexte1(text1);
                    couple.setTexte2(text2);
                    couple.setDataset(managedDataset);

                    batch.add(couple);
                    rowCount++;

                    logger.debug("Added couple {}: '{}' - '{}'", rowCount,
                               text1.length() > 50 ? text1.substring(0, 50) + "..." : text1,
                               text2.length() > 50 ? text2.substring(0, 50) + "..." : text2);

                    if (batch.size() == batchSize) {
                        coupleTextService.saveBatch(batch);
                        logger.info("Saved batch of {} couples (total processed: {})", batch.size(), rowCount);
                        batch.clear();
                    }
                }
            }
        }

        return rowCount;
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        result.add(currentField.toString());
        return result.toArray(new String[0]);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }





    @Override
    @Transactional
    public Dataset createDataset(String name, String description, MultipartFile file, String classesRaw) throws IOException {
        logger.info("Creating dataset: name='{}', description='{}', file='{}', classes='{}'",
                   name, description, file != null ? file.getOriginalFilename() : "null", classesRaw);

        try {
            // Validation
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Dataset name cannot be null or empty");
            }

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be null or empty");
            }

            if (classesRaw == null || classesRaw.trim().isEmpty()) {
                throw new IllegalArgumentException("Classes cannot be null or empty");
            }

            Dataset dataset = new Dataset();
            dataset.setName(name.trim());
            dataset.setDescription(description != null ? description.trim() : "");

            logger.info("Processing file upload...");

            // Create upload directory if it doesn't exist
            File uploadDirFile = new File(UPLOAD_DIR);
            if (!uploadDirFile.exists()) {
                logger.info("Creating upload directory: {}", uploadDirFile.getAbsolutePath());
                boolean created = uploadDirFile.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create upload directory: " + uploadDirFile.getAbsolutePath());
                }
            }

            // Generate a unique filename to avoid collisions
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new IllegalArgumentException("File must have a valid filename");
            }

            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
            logger.info("Generated unique filename: {}", uniqueFilename);

            // Create the complete file path
            Path targetLocation = Paths.get(UPLOAD_DIR, uniqueFilename).toAbsolutePath();
            logger.info("Target file location: {}", targetLocation);

            // Actually save the file to disk
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File saved successfully to: {}", targetLocation);

            // Store the absolute path in the dataset
            dataset.setFilePath(targetLocation.toString());
            dataset.setFileType(file.getContentType());

            logger.info("Processing classes...");

            // Handle classes
            Set<ClassPossible> classSet = Arrays.stream(classesRaw.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(className -> {
                        logger.debug("Creating class: {}", className);
                        ClassPossible cp = new ClassPossible();
                        cp.setTextClass(className);
                        cp.setDataset(dataset);
                        return cp;
                    })
                    .collect(Collectors.toSet());

            if (classSet.isEmpty()) {
                throw new IllegalArgumentException("At least one class must be provided");
            }

            dataset.setClassesPossibles(classSet);
            logger.info("Created {} classes for dataset", classSet.size());

            // Save the dataset to get an ID assigned
            logger.info("Saving dataset to database...");
            Dataset savedDataset = datasetRepository.save(dataset);
            logger.info("Dataset saved successfully with ID: {}", savedDataset.getId());

            return savedDataset;

        } catch (Exception e) {
            logger.error("Error creating dataset: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public void SaveDataset(Dataset dataset) {
        datasetRepository.save(dataset);
    }

    @Override
    @Transactional
    public void deleteDataset(Long id) {
        Dataset dataset = datasetRepository.findById(id).orElse(null);
        if (dataset == null) {
            return;
        }
        
        // Supprimer les couples de textes associés au dataset
        if (dataset.getCoupleTexts() != null && !dataset.getCoupleTexts().isEmpty()) {
            coupleTextRepository.deleteAll(dataset.getCoupleTexts());
        }
        
        // Supprimer le dataset
        datasetRepository.deleteById(id);
    }

    @Override
    public long countDatasets() {
        return datasetRepository.count();
    }

    @Override
    public List<Long> getAssignedAnnotatorIds(Long datasetId) {
        try {
            // Use a query to get assigned annotator IDs without loading the full entities
            return tacheRepository.findAnnotatorIdsByDatasetId(datasetId);
        } catch (Exception e) {
            logger.error("Error getting assigned annotator IDs for dataset {}: {}", datasetId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Date getDatasetDeadline(Long datasetId) {
        try {
            // Get the first deadline from tasks for this dataset
            List<Date> deadlines = tacheRepository.findDeadlinesByDatasetId(datasetId);
            return deadlines.isEmpty() ? null : deadlines.get(0);
        } catch (Exception e) {
            logger.error("Error getting deadline for dataset {}: {}", datasetId, e.getMessage());
            return null;
        }
    }

}
