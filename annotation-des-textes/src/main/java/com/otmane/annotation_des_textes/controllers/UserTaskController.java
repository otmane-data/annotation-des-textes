package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.*;
import com.otmane.annotation_des_textes.services.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserTaskController {
    private final AnnotateurService annotateurService;
    private final TacheService tacheService;
    private final UtilisateurService utilisateurService;
    private final TaskProgressService taskProgressService;
    private final AnnotationService annotationService;

    public UserTaskController(AnnotateurService annotateurService, TacheService tacheService, 
                            UtilisateurService utilisateurService, TaskProgressService taskProgressService, 
                            AnnotationService annotationService) {
        this.annotateurService = annotateurService;
        this.tacheService = tacheService;
        this.utilisateurService = utilisateurService;
        this.taskProgressService = taskProgressService;
        this.annotationService = annotationService;
    }

    private Annotateur getCurrentAnnotateur() {
        Long utilisateurId = utilisateurService.getCurrentUtilisateurId();
        if (utilisateurId == null) {
            return null;
        }
        return annotateurService.findAnnotateurById(utilisateurId);
    }

    @GetMapping("/tasks")
    public String showUserTaskHome(Model model) {
        Annotateur annotateur = getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }

        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        List<Tache> taches = tacheService.getValidTasksForAnnotateur(annotateur.getId());

        // Map task ID → lastIndex
        Map<Long, Float> taskProgressMap = new HashMap<>();

        for (Tache tache : taches) {
            Optional<ProgressTache> progressOpt = taskProgressService.getProgressForUserAndTask(annotateur, tache.getId());
            taskProgressMap.put(tache.getId(), Float.valueOf(progressOpt.map(ProgressTache::getDernierIndex).orElse(0)));
        }

        model.addAttribute("tasks", taches);
        model.addAttribute("taskProgressMap", taskProgressMap);
        model.addAttribute("currentUserName", currentUserName);
        return "user/tasks";
    }

    /**
     * Display the task detail with the current couple to annotate
     */
    @GetMapping("/tasks/{id}")
    public String viewTaskDetail(@PathVariable Long id,
                                 @RequestParam(required = false, defaultValue = "0") Integer index,
                                 Model model) {
        Annotateur annotateur = getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }

        // Get the task and verify it belongs to this user
        Tache tache = tacheService.findTaskById(id);
        if (!tache.getAnnotateur().getId().equals(annotateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this task");
        }

        // Get all couples for this task
        List<CoupleText> couples = new ArrayList<>(tache.getCouples());
        int totalCouples = couples.size();

        // Validate the index
        if (index < 0) {
            index = 0;
        } else if (index >= totalCouples) {
            index = totalCouples - 1;
        }
        if (index == null || index == 0) {
            Optional<ProgressTache> progressOpt = taskProgressService.getProgressForUserAndTask(annotateur, id);
            if (progressOpt.isPresent()) {
                index = progressOpt.get().getDernierIndex();
            }
        }

        // Get the current couple
        CoupleText currentCouple = null;
        if (!couples.isEmpty()) {
            currentCouple = couples.get(index);
        }

        // Check if this couple already has an annotation from this user
        String selectedClassId = tacheService.getSelectedClassId(tache.getId(), currentCouple.getId(), annotateur.getId());
        
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("task", tache);
        model.addAttribute("currentCouple", currentCouple);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalCouples", totalCouples);
        model.addAttribute("selectedClassId", selectedClassId);

        return "user/task_view";
    }

    /**
     * Handle the annotation submission
     */
    @PostMapping("/tasks/{taskId}/annotate")
    public String annotateCouple(@PathVariable Long taskId,
                                 @RequestParam Long coupleId,
                                 @RequestParam String classSelection,
                                 @RequestParam(required = false) String notes,
                                 @RequestParam Integer currentIndex,
                                 RedirectAttributes redirectAttributes) {
        Annotateur annotateur = getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }

        // Save the annotation
        annotationService.saveAnnotation(classSelection, coupleId, annotateur.getId());

        int nextIndex = currentIndex + 1;
        taskProgressService.saveOrUpdateProgress(annotateur, taskId, nextIndex);
        redirectAttributes.addFlashAttribute("successMessage", "Annotation saved successfully!");

        Tache tache = tacheService.findTaskById(taskId);
        if (tache != null && nextIndex >= tache.getCouples().size()) {
            redirectAttributes.addFlashAttribute("completedMessage", "Congratulations! You have completed all annotations for this task.");
            return "redirect:/user/home";
        }

        return "redirect:/user/tasks/" + taskId + "?index=" + nextIndex;
    }

    @GetMapping("/history")
    public String showUserHistory(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model) {
        Annotateur annotateur = getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }
        
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        
        Page<Annotation> annotationsPage = annotationService.findAllAnnotationsByUser(annotateur, PageRequest.of(page, size));
        model.addAttribute("annotations", annotationsPage.getContent());
        model.addAttribute("currentPage", annotationsPage.getNumber());
        model.addAttribute("totalPages", annotationsPage.getTotalPages());
        model.addAttribute("totalItems", annotationsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentUserName", currentUserName);
        return "user/history";
    }
}