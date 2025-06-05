package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.*;
import com.otmane.annotation_des_textes.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/task-management")
public class TaskManagementController {
    private final TacheService tacheService;
    private final UtilisateurService utilisateurService;
    private final TaskProgressService taskProgressService;
    private final AnnotationService annotationService;
    private final AnnotateurService annotateurService;

    public TaskManagementController(
            TacheService tacheService,
            UtilisateurService utilisateurService,
            TaskProgressService taskProgressService,
            AnnotationService annotationService,
            AnnotateurService annotateurService) {
        this.tacheService = tacheService;
        this.utilisateurService = utilisateurService;
        this.taskProgressService = taskProgressService;
        this.annotationService = annotationService;
        this.annotateurService = annotateurService;
    }

    @GetMapping("/overview")
    public String showTaskOverview(Model model) {
        String currentUserName = capitalize(utilisateurService.getCurrentUserName());
        List<Tache> allTasks = tacheService.findAllTasks();
        
        Map<Long, TaskOverviewData> taskOverviewMap = new HashMap<>();
        
        for (Tache task : allTasks) {
            TaskOverviewData overviewData = new TaskOverviewData();
            overviewData.setTask(task);
            overviewData.setProgress(taskProgressService.calculateProgressPercentage(task, task.getAnnotateur()));
            overviewData.setTotalAnnotations(annotationService.findAnnotationsByTaskId(task.getId()).size());
            overviewData.setDeadlineStatus(calculateDeadlineStatus(task.getDateLimite()));
            
            taskOverviewMap.put(task.getId(), overviewData);
        }
        
        // Get all annotators for reassignment dropdown
        List<Annotateur> annotateurs = annotateurService.findAllActive();

        model.addAttribute("taskOverviewMap", taskOverviewMap);
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("annotateurs", annotateurs);
        return "admin/task_management/overview";
    }

    @PostMapping("/{taskId}/extend-deadline")
    public String extendDeadline(
            @PathVariable Long taskId,
            @RequestParam Date newDeadline,
            RedirectAttributes redirectAttributes) {
        try {
            Tache task = tacheService.findTaskById(taskId);
            task.setDateLimite(newDeadline);
            tacheService.save(task);
            redirectAttributes.addFlashAttribute("success", "Deadline extended successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to extend deadline: " + e.getMessage());
        }
        return "redirect:/admin/task-management/overview";
    }

    @PostMapping("/{taskId}/reassign")
    public String reassignTask(
            @PathVariable Long taskId,
            @RequestParam Long newAnnotatorId,
            RedirectAttributes redirectAttributes) {
        try {
            Tache task = tacheService.findTaskById(taskId);
            Annotateur newAnnotator = annotateurService.findAnnotateurById(newAnnotatorId);
            
            task.setAnnotateur(newAnnotator);
            tacheService.save(task);
            
            redirectAttributes.addFlashAttribute("success", "Task reassigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reassign task: " + e.getMessage());
        }
        return "redirect:/admin/task-management/overview";
    }

    private String calculateDeadlineStatus(Date deadline) {
        if (deadline == null) return "No Deadline";

        Date now = new Date();
        long daysUntilDeadline = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);

        if (deadline.before(now)) return "Expired";
        if (daysUntilDeadline <= 7) return "Approaching";
        return "On Track";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private static class TaskOverviewData {
        private Tache task;
        private int progress;
        private int totalAnnotations;
        private String deadlineStatus;

        // Getters and setters
        public Tache getTask() { return task; }
        public void setTask(Tache task) { this.task = task; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getTotalAnnotations() { return totalAnnotations; }
        public void setTotalAnnotations(int totalAnnotations) { this.totalAnnotations = totalAnnotations; }
        public String getDeadlineStatus() { return deadlineStatus; }
        public void setDeadlineStatus(String deadlineStatus) { this.deadlineStatus = deadlineStatus; }
    }
}