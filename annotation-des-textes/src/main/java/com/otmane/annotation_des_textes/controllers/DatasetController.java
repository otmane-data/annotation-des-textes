package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.services.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin")
public class DatasetController {

    private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);

    private final DatasetService datasetService;
    private final AnnotateurService annotateurService;
    private final CoupleTextService coupleTextService;
    private final AsyncDatasetParserService asyncDatasetParserService;
    private final UtilisateurService utilisateurService;
    private final AssignTaskToAnnotator assignTaskToAnnotator;
    private final TacheService tacheService;

    // Constructor-based injection for all services
    @Autowired
    public DatasetController(DatasetService datasetService, AnnotateurService annotateurService, CoupleTextService coupleTextService, AsyncDatasetParserService asyncDatasetParserService, UtilisateurService utilisateurService, AssignTaskToAnnotator assignTaskToAnnotator, TacheService tacheService) {
        this.datasetService = datasetService;
        this.annotateurService = annotateurService;
        this.coupleTextService = coupleTextService;
        this.asyncDatasetParserService = asyncDatasetParserService;
        this.utilisateurService = utilisateurService;
        this.assignTaskToAnnotator = assignTaskToAnnotator;
        this.tacheService = tacheService;
    }

    @GetMapping("/datasets")
    public String showDatasetHome(Model model) {
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("datasets", datasetService.findAllDatasets());
        return "admin/datasets_management/datasets";
    }

    @GetMapping("/datasets/details/{id}")
    public String DatasetDetails(@PathVariable Long id, Model model,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "25") int size) {

        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);

        logger.info("Loading dataset details for id: {}", id);
        Dataset dataset = datasetService.findDatasetById(id);

        if (dataset == null) {
            logger.warn("Dataset not found with id: {}", id);
            model.addAttribute("errorMessage", "Dataset not found");
            return "redirect:/admin/datasets";
        }

        logger.info("Dataset found: {} (id: {})", dataset.getName(), dataset.getId());

        // Debug: Check dataset's coupleTexts collection
        if (dataset.getCoupleTexts() != null) {
            logger.info("Dataset has {} couples in collection", dataset.getCoupleTexts().size());
        } else {
            logger.warn("Dataset coupleTexts collection is null");
        }

        // Get couples via service
        Page<CoupleText> coupleTextsPage = coupleTextService.getCoupleTextsByDatasetId(id, page, size);
        logger.info("Retrieved {} couples from service (total: {})",
                   coupleTextsPage.getNumberOfElements(),
                   coupleTextsPage.getTotalElements());

        // Debug: Check if couples exist directly
        long directCount = coupleTextService.countCoupleTextsByDatasetId(id);
        logger.info("Direct count of couples for dataset {}: {}", id, directCount);

        int totalPages = coupleTextsPage.getTotalPages();
        int currentPage = page;

        // Pagination window control (show up to 5 pages max, centered around current)
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);

        model.addAttribute("coupleTextsPage", coupleTextsPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("dataset", dataset);

        logger.info("Successfully loaded dataset details for id {}, page {}, total couples: {}",
                   id, page, coupleTextsPage.getTotalElements());
        return "admin/datasets_management/dataset_details";
    }


    @GetMapping("/datasets/add")
    public String addDataset(Model model) {
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("dataset", new Dataset());
        return "admin/datasets_management/addDataset";
    }

    @GetMapping("/datasets/add-simple")
    public String addDatasetSimple(Model model) {
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("dataset", new Dataset());
        return "admin/datasets_management/addDatasetSimple";
    }

    @GetMapping("/datasets/{id}/parse-sync")
    public String parseDatasetSync(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Manual sync parsing requested for dataset ID: {}", id);
            Dataset dataset = datasetService.findDatasetById(id);

            if (dataset == null) {
                redirectAttributes.addFlashAttribute("error", "Dataset not found");
                return "redirect:/admin/datasets";
            }

            logger.info("Starting synchronous parsing for dataset: {}", dataset.getName());
            datasetService.ParseDataset(dataset);

            redirectAttributes.addFlashAttribute("success", "Dataset parsed successfully!");

        } catch (Exception e) {
            logger.error("Error during sync parsing: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Parsing failed: " + e.getMessage());
        }

        return "redirect:/admin/datasets/details/" + id;
    }

    @GetMapping("/datasets/{id}/debug")
    @ResponseBody
    public String debugDataset(@PathVariable Long id) {
        StringBuilder debug = new StringBuilder();
        debug.append("=== DEBUG DATASET ").append(id).append(" ===\n");

        try {
            // Check dataset exists
            Dataset dataset = datasetService.findDatasetById(id);
            if (dataset == null) {
                debug.append("❌ Dataset not found\n");
                return debug.toString();
            }

            debug.append("✅ Dataset found: ").append(dataset.getName()).append("\n");
            debug.append("📁 File path: ").append(dataset.getFilePath()).append("\n");

            // Check file exists
            if (dataset.getFilePath() != null) {
                File file = new File(dataset.getFilePath());
                debug.append("📄 File exists: ").append(file.exists()).append("\n");
                debug.append("📏 File size: ").append(file.length()).append(" bytes\n");
            }

            // Check couples count
            long count = coupleTextService.countCoupleTextsByDatasetId(id);
            debug.append("🔢 Total couples in DB: ").append(count).append("\n");

            // Get first few couples
            List<CoupleText> couples = coupleTextService.findAllCoupleTextsByDatasetId(id);
            debug.append("📝 Couples found by findAll: ").append(couples.size()).append("\n");

            if (!couples.isEmpty()) {
                debug.append("📋 First couple: '").append(couples.get(0).getTexte1())
                     .append("' - '").append(couples.get(0).getTexte2()).append("'\n");
            }

            // Test pagination method
            Page<CoupleText> page = coupleTextService.getCoupleTextsByDatasetId(id, 0, 10);
            debug.append("📄 Couples found by pagination: ").append(page.getTotalElements()).append("\n");

        } catch (Exception e) {
            debug.append("❌ Error: ").append(e.getMessage()).append("\n");
            debug.append("Stack trace: ").append(e.getClass().getSimpleName()).append("\n");
        }

        return debug.toString();
    }

    @PostMapping("/datasets/save")
    public String saveDataset(@ModelAttribute Dataset dataset,
                              @RequestParam("classes") String classesRaw,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {

        logger.info("=== DATASET CREATION STARTED ===");
        logger.info("Received parameters:");
        logger.info("- Dataset name: {}", dataset != null ? dataset.getName() : "NULL");
        logger.info("- Dataset description: {}", dataset != null ? dataset.getDescription() : "NULL");
        logger.info("- File: {}", file != null ? file.getOriginalFilename() : "NULL");
        logger.info("- File size: {}", file != null ? file.getSize() : "NULL");
        logger.info("- Classes: {}", classesRaw);

        try {
            logger.info("Starting dataset creation: name={}, description={}, file={}, classes={}",
                       dataset.getName(), dataset.getDescription(), file.getOriginalFilename(), classesRaw);

            // Validation des paramètres
            if (dataset.getName() == null || dataset.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Dataset name cannot be empty");
            }

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            if (classesRaw == null || classesRaw.trim().isEmpty()) {
                throw new IllegalArgumentException("Classes cannot be empty");
            }

            logger.info("Creating dataset with service...");
            Dataset savedDataset = datasetService.createDataset(dataset.getName(), dataset.getDescription(), file, classesRaw);

            logger.info("Saving dataset...");
            datasetService.SaveDataset(savedDataset);

            logger.info("Starting parsing...");
            logger.info("Dataset file path: {}", savedDataset.getFilePath());
            logger.info("Dataset ID: {}", savedDataset.getId());

            // Verify file exists before parsing
            if (savedDataset.getFilePath() != null) {
                File savedFile = new File(savedDataset.getFilePath());
                logger.info("File exists: {}, File size: {}", savedFile.exists(), savedFile.length());
            }

            // Try synchronous parsing first to debug
            try {
                logger.info("Attempting synchronous parsing for debugging...");
                datasetService.ParseDataset(savedDataset);
                logger.info("Synchronous parsing completed successfully");
            } catch (Exception e) {
                logger.error("Synchronous parsing failed: {}", e.getMessage(), e);
                // Fall back to async if sync fails
                logger.info("Falling back to async parsing...");
                asyncDatasetParserService.parseDatasetAsync(savedDataset);
            }

            logger.info("Dataset created successfully with id: {}", savedDataset.getId());
            redirectAttributes.addFlashAttribute("success", "Dataset added successfully. Text pairs are being processed...");

        } catch (IOException e) {
            logger.error("IO error while creating dataset: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to upload dataset: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Validation error while creating dataset: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while creating dataset: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Unexpected error: " + e.getMessage());
            // Print full stack trace for debugging
            e.printStackTrace();
        }

        return "redirect:/admin/datasets";
    }

    @GetMapping("/datasets/{id}/assign_annotator")
    public String assignAnnotator(Model model,
                                  @PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        // Retrieve current user for display
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);

        // Fetch active annotators
        List<Annotateur> annotateurs = annotateurService.findAllActive();

        // Proceed normally if enough annotators
        Dataset dataset = datasetService.findDatasetById(id);

        // Get already assigned annotators using service method to avoid LazyInitializationException
        List<Long> assignedAnnotateurIds = new ArrayList<>();
        try {
            assignedAnnotateurIds = datasetService.getAssignedAnnotatorIds(id);
            logger.info("Found {} assigned annotators for dataset {}", assignedAnnotateurIds.size(), id);
        } catch (Exception e) {
            logger.error("Error getting assigned annotators: {}", e.getMessage(), e);
            // Continue with empty list
        }

        // Get deadline from existing tasks using service method
        Date deadlineDate = null;
        try {
            deadlineDate = datasetService.getDatasetDeadline(id);
        } catch (Exception e) {
            logger.error("Error getting deadline: {}", e.getMessage(), e);
            // Continue with null deadline
        }

        // Add attributes to model
        model.addAttribute("deadlineDate", deadlineDate);
        model.addAttribute("assignedAnnotateurIds", assignedAnnotateurIds);
        model.addAttribute("dataset", dataset);
        model.addAttribute("annotateurs", annotateurs);

        return "admin/datasets_management/annotateur_assignment";
    }
    @GetMapping("/datasets/{id}/unassign_annotator")
    public String unassignAnnotator(@PathVariable Long id,
                                    @RequestParam Long annotatorId,
                                    RedirectAttributes redirectAttributes) {
        try {
            assignTaskToAnnotator.unassignAnnotator(id, annotatorId);
            redirectAttributes.addFlashAttribute("success", "Annotator unassigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to unassign annotator: " + e.getMessage());
        }
        return "redirect:/admin/datasets/details/" + id;
    }
    
    @GetMapping("/datasets/delete/{id}")
    public String deleteDataset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si le dataset existe
            Dataset dataset = datasetService.findDatasetById(id);
            if (dataset == null) {
                redirectAttributes.addFlashAttribute("error", "Dataset not found");
                return "redirect:/admin/datasets";
            }
            
            // Supprimer le dataset
            datasetService.deleteDataset(id);
            redirectAttributes.addFlashAttribute("success", "Dataset deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting dataset: " + e.getMessage());
        }
        return "redirect:/admin/datasets";
    }

    @GetMapping("/datasets/{id}/debug-tasks")
    @ResponseBody
    public String debugTasks(@PathVariable Long id) {
        try {
            Dataset dataset = datasetService.findDatasetById(id);
            if (dataset == null) {
                return "Dataset not found with ID: " + id;
            }

            // Get all tasks for this dataset
            List<Tache> tasks = tacheService.findTasksByDatasetId(id);

            StringBuilder debug = new StringBuilder();
            debug.append("=== DEBUG TASKS FOR DATASET ").append(id).append(" ===\n");
            debug.append("Dataset: ").append(dataset.getName()).append("\n");
            debug.append("Total tasks found: ").append(tasks.size()).append("\n\n");

            for (int i = 0; i < tasks.size(); i++) {
                Tache task = tasks.get(i);
                debug.append("Task ").append(i + 1).append(":\n");
                debug.append("  - ID: ").append(task.getId()).append("\n");
                debug.append("  - Annotator: ").append(task.getAnnotateur() != null ?
                    task.getAnnotateur().getNom() + " " + task.getAnnotateur().getPrenom() : "NULL").append("\n");
                debug.append("  - Annotator ID: ").append(task.getAnnotateur() != null ?
                    task.getAnnotateur().getId() : "NULL").append("\n");
                debug.append("  - Deadline: ").append(task.getDateLimite()).append("\n");
                debug.append("  - Couples count: ").append(task.getCouples() != null ?
                    task.getCouples().size() : "NULL").append("\n");
                debug.append("\n");
            }

            return debug.toString();

        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n" + e.getStackTrace()[0];
        }
    }

    @GetMapping("/datasets/{id}/debug-assignment")
    @ResponseBody
    public String debugAssignment(@PathVariable Long id) {
        StringBuilder debug = new StringBuilder();
        debug.append("=== DEBUG ASSIGNMENT FOR DATASET ").append(id).append(" ===\n");

        try {
            // Check dataset exists
            Dataset dataset = datasetService.findDatasetById(id);
            if (dataset == null) {
                debug.append("❌ Dataset not found\n");
                return debug.toString();
            }
            debug.append("✅ Dataset found: ").append(dataset.getName()).append("\n");

            // Check couples count
            long couplesCount = coupleTextService.countCoupleTextsByDatasetId(id);
            debug.append("📝 Couples in dataset: ").append(couplesCount).append("\n");

            if (couplesCount == 0) {
                debug.append("❌ No couples found - cannot assign tasks\n");
                return debug.toString();
            }

            // Check annotators
            List<Annotateur> annotators = annotateurService.findAllActive();
            debug.append("👥 Active annotators: ").append(annotators.size()).append("\n");

            if (annotators.size() < 3) {
                debug.append("❌ Less than 3 active annotators\n");
                return debug.toString();
            }

            // Test assignment with first 3 annotators
            List<Annotateur> testAnnotators = annotators.subList(0, 3);
            debug.append("🧪 Testing assignment with annotators: ");
            for (Annotateur ann : testAnnotators) {
                debug.append(ann.getNom()).append(" ");
            }
            debug.append("\n");

            // Create test deadline (tomorrow)
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            Date testDeadline = cal.getTime();
            debug.append("📅 Test deadline: ").append(testDeadline).append("\n");

            // Try assignment
            debug.append("🚀 Starting test assignment...\n");
            assignTaskToAnnotator.assignTaskToAnnotator(testAnnotators, dataset, testDeadline);
            debug.append("✅ Assignment completed without exception\n");

            // Check if tasks were created
            List<Tache> tasks = tacheService.findTasksByDatasetId(id);
            debug.append("📊 Tasks created: ").append(tasks.size()).append("\n");

            for (Tache task : tasks) {
                debug.append("  - Task ID ").append(task.getId())
                     .append(", Annotator: ").append(task.getAnnotateur().getNom())
                     .append(", Couples: ").append(task.getCouples().size())
                     .append(", Deadline: ").append(task.getDateLimite()).append("\n");
            }

        } catch (Exception e) {
            debug.append("❌ Error during assignment test: ").append(e.getMessage()).append("\n");
            debug.append("Stack trace: ").append(e.getClass().getSimpleName()).append("\n");
            e.printStackTrace();
        }

        return debug.toString();
    }

    @GetMapping("/datasets/{id}/test-simple-task")
    @ResponseBody
    public String testSimpleTask(@PathVariable Long id) {
        StringBuilder result = new StringBuilder();
        result.append("=== TEST SIMPLE CREATION TACHE ===\n");

        try {
            // Get dataset
            Dataset dataset = datasetService.findDatasetById(id);
            if (dataset == null) {
                result.append("❌ Dataset not found\n");
                return result.toString();
            }

            // Get first annotator
            List<Annotateur> annotators = annotateurService.findAllActive();
            if (annotators.isEmpty()) {
                result.append("❌ No active annotators\n");
                return result.toString();
            }

            Annotateur annotator = annotators.get(0);
            result.append("✅ Using annotator: ").append(annotator.getNom()).append("\n");

            // Create simple task
            Tache tache = new Tache();
            tache.setAnnotateur(annotator);
            tache.setDataset(dataset);
            tache.setDateLimite(new Date(System.currentTimeMillis() + 86400000)); // Tomorrow

            result.append("🚀 Saving simple task...\n");
            Tache saved = tacheService.save(tache);
            result.append("✅ Task saved with ID: ").append(saved.getId()).append("\n");

            // Verify in database
            boolean exists = tacheService.existsById(saved.getId());
            result.append("📊 Task exists in DB: ").append(exists).append("\n");

            // Count total tasks
            long totalTasks = tacheService.count();
            result.append("📈 Total tasks in database: ").append(totalTasks).append("\n");

        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage()).append("\n");
            result.append("Exception: ").append(e.getClass().getSimpleName()).append("\n");
            e.printStackTrace();
        }

        return result.toString();
    }
}