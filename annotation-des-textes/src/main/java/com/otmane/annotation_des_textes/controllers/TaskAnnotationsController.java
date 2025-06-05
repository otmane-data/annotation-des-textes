package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.*;
import com.otmane.annotation_des_textes.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class TaskAnnotationsController {
    private final TacheService tacheService;
    private final UtilisateurService utilisateurService;
    private final TaskProgressService taskProgressService;
    private final AnnotationService annotationService;

    public TaskAnnotationsController(TacheService tacheService, UtilisateurService utilisateurService, TaskProgressService taskProgressService, AnnotationService annotationService) {
        this.tacheService = tacheService;
        this.utilisateurService = utilisateurService;
        this.taskProgressService = taskProgressService;
        this.annotationService = annotationService;
    }


    @GetMapping("/tasks")
    public String showAdminTaskHome(Model model) {
        // Redirect to the new task management interface
        return "redirect:/admin/task-management/overview";
    }

    @GetMapping("/tasks/legacy")
    public String showLegacyAdminTaskHome(Model model) {
        String currentUserName = utilisateurService.getCurrentUserName();
        if (currentUserName != null && !currentUserName.isEmpty()) {
            currentUserName = currentUserName.substring(0, 1).toUpperCase() + currentUserName.substring(1);
        }

        List<Tache> taches = tacheService.findAllTasks();
        List<Map<String, Object>> tasksWithProgress = new ArrayList<>();

        for (Tache tache : taches) {
            Annotateur annotateur = tache.getAnnotateur();
            Long taskId = tache.getId();

            Optional<ProgressTache> progressOpt = taskProgressService.getProgressForUserAndTask(annotateur, taskId);
            int done = progressOpt.map(ProgressTache::getDernierIndex).orElse(0);
            int total = tache.getCouples().size(); // ou tache.getDataset().getCouples().size()

            Map<String, Object> data = new HashMap<>();
            data.put("task", tache);
            data.put("annotatorName", annotateur.getLogin());
            data.put("done", done);
            data.put("total", total);
            data.put("percent", total == 0 ? 0 : (int)((done * 100.0) / total));

            tasksWithProgress.add(data);
        }

        model.addAttribute("tasksWithProgress", tasksWithProgress);
        model.addAttribute("currentUserName", currentUserName);

        return "admin/tasks_management/task_view";
    }


    /**
     * Display all annotations for a specific task
     */
    @GetMapping("/annotations/{taskId}")
    public String viewTaskAnnotations(@PathVariable Long taskId, Model model) {
        // Get the task
        Tache tache = tacheService.findTaskById(taskId);

        // Get the annotator
        Annotateur annotateur = tache.getAnnotateur(); // Or get from session/user context

        // Get the annotations done by that annotator on that specific task
        List<Annotation> annotations = annotationService.findAnnotationsByAnnotatorForTask(taskId, annotateur.getId());

        int completedCount = annotations.size();
        int totalCouples = tache.getCouples().size();

        String currentUserName = utilisateurService.getCurrentUserName();
        if (currentUserName != null && !currentUserName.isEmpty()) {
            currentUserName = currentUserName.substring(0, 1).toUpperCase() + currentUserName.substring(1);
        }

        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("task", tache);
        model.addAttribute("annotations", annotations);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCouples", totalCouples);

        return "admin/tasks_management/task_annotations";
    }
}