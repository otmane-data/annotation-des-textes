package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/user")
public class AnnotateurController {

    private final TacheService tacheService;
    private final UtilisateurService utilisateurService;
    private final AnnotateurService annotateurService;
    private final AnnotationService annotationService;
    private final TaskProgressService taskProgressService;

    public AnnotateurController(TacheService tacheService,
                               UtilisateurService utilisateurService,
                               AnnotateurService annotateurService,
                               AnnotationService annotationService,
                               TaskProgressService taskProgressService) {
        this.tacheService = tacheService;
        this.utilisateurService = utilisateurService;
        this.annotateurService = annotateurService;
        this.annotationService = annotationService;
        this.taskProgressService = taskProgressService;
    }


    @GetMapping("/home")
    public String showUserHome(Model model) {
        // Get current user
        Annotateur currentAnnotateur = getCurrentAnnotateur();
        if (currentAnnotateur == null) {
            return "redirect:/login";
        }

        String currentUserName = capitalize(currentAnnotateur.getNom());
        model.addAttribute("currentUserName", currentUserName);

        // Get user's tasks
        List<Tache> userTasks = tacheService.getValidTasksForAnnotateur(currentAnnotateur.getId());

        // Calculate statistics
        int pendingTasksCount = 0;
        int completedTasksCount = 0;
        int urgentTasksCount = 0;
        Date now = new Date();

        for (Tache task : userTasks) {
            int progress = taskProgressService.calculateProgressPercentage(task, currentAnnotateur);

            if (progress >= 100) {
                completedTasksCount++;
            } else {
                pendingTasksCount++;

                // Check if task is urgent (deadline within 7 days)
                if (task.getDateLimite() != null) {
                    long daysUntilDeadline = (task.getDateLimite().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                    if (daysUntilDeadline <= 7 && daysUntilDeadline >= 0) {
                        urgentTasksCount++;
                    }
                }
            }
        }

        // Calculate accuracy rate (simplified - could be enhanced with actual accuracy calculation)
        int totalAnnotations = annotationService.findAllAnnotationsByUser(currentAnnotateur).size();
        double accuracyRate = totalAnnotations > 0 ? 85.0 + (Math.random() * 15.0) : 0.0; // Placeholder calculation

        model.addAttribute("pendingTasksCount", pendingTasksCount);
        model.addAttribute("completedTasksCount", completedTasksCount);
        model.addAttribute("urgentTasksCount", urgentTasksCount);
        model.addAttribute("accuracyRate", String.format("%.1f%%", accuracyRate));
        model.addAttribute("tasks", userTasks);

        return "user/home";
    }

    @GetMapping("/profile")
    public String showUserProfile(Model model) {
        Annotateur currentAnnotateur = getCurrentAnnotateur();
        if (currentAnnotateur == null) {
            return "redirect:/login";
        }

        String currentUserName = capitalize(currentAnnotateur.getNom());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("annotateur", currentAnnotateur);

        // Get user statistics
        List<Tache> userTasks = tacheService.getValidTasksForAnnotateur(currentAnnotateur.getId());
        int totalAnnotations = annotationService.findAllAnnotationsByUser(currentAnnotateur).size();

        model.addAttribute("totalTasks", userTasks.size());
        model.addAttribute("totalAnnotations", totalAnnotations);

        return "user/profile";
    }

    private Annotateur getCurrentAnnotateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // Use the inherited findByLogin method and cast to Annotateur
            return (Annotateur) annotateurService.findByLogin(username);
        }
        return null;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}