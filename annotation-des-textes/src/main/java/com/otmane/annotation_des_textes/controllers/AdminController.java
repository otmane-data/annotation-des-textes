package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.*;
import com.otmane.annotation_des_textes.repositories.RoleRepository;
import com.otmane.annotation_des_textes.services.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UtilisateurService utilisateurService;
    private final AnnotateurService annotateurService;
    private final RoleRepository roleRepository;
    private final TacheService tacheService;
    private final DatasetService datasetService;
    private final AnnotationService annotationService;
    private final TaskProgressService taskProgressService;
    private final PasswordService passwordService;

    @Autowired
    public AdminController(
            UtilisateurService utilisateurService, AnnotateurService annotateurService,
            RoleRepository roleRepository, TacheService tacheService, DatasetService datasetService, 
            AnnotationService annotationService, TaskProgressService taskProgressService, 
            PasswordService passwordService) {
        this.utilisateurService = utilisateurService;
        this.annotateurService = annotateurService;
        this.roleRepository = roleRepository;
        this.tacheService = tacheService;
        this.datasetService = datasetService;
        this.annotationService = annotationService;
        this.taskProgressService = taskProgressService;
        this.passwordService = passwordService;
    }

    @GetMapping("/showAdminHome")
    public String showAdminHome(Model model) {
        long totalAnnotations = annotationService.countTotalAnnotations();
        long activeTasks = tacheService.countActiveTasks();
        long totalDatasets = datasetService.countDatasets();
        long totalAnnotateurs = annotateurService.findAllActive().size();
        double taskCompletionPercentage = taskProgressService.calculateOverallTaskCompletionPercentage();
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("totalAnnotations", totalAnnotations);
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("totalDatasets", totalDatasets);
        model.addAttribute("totalAnnotateurs", totalAnnotateurs);
        model.addAttribute("taskCompletionPercentage", taskCompletionPercentage);

        return "admin/adminHome";
    }

    @GetMapping("/annotateurs")
    public String showUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model) {
        Page<Annotateur> annotateurs = annotateurService.findAllActive(page, size);
        Map<Long, LocalDateTime> lastActivity = new HashMap<>();
        long totalAnnotateurs = annotateurService.countActiveAnnotateurs();

        for (Annotateur annotateur : annotateurs) {
            ProgressTache latestProgress = taskProgressService.getLastAnnotationByUser(annotateur);

            if (latestProgress != null) {
                lastActivity.put(annotateur.getId(), latestProgress.getDerniereMiseAJour());
            } else {
                lastActivity.put(annotateur.getId(), null);
            }
        }
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("lastActivity", lastActivity);
        model.addAttribute("annotateurs", annotateurs);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", annotateurs.getTotalPages());
        model.addAttribute("totalItems", totalAnnotateurs);
        return "admin/annotateur_management/annotateurs";
    }

    @GetMapping("/annotateurs/add")
    public String addAnnotateur(Model model) {
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("user", new Annotateur());
        return "admin/annotateur_management/addAnnotateur";
    }

    @GetMapping("/annotateurs/update/{id}")
    public String updateAnnotateur(@PathVariable Long id, Model model) {
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        Annotateur annotateur = annotateurService.findAnnotateurById(id);
        if (annotateur == null) {
            model.addAttribute("errorMessage", "Annotateur not found");
            return "redirect:/admin/annotateurs";
        }
        model.addAttribute("user", annotateur);
        return "admin/annotateur_management/addAnnotateur";
    }



    @PostMapping("/annotateurs/save")
    public String saveAnnotateur(@ModelAttribute Annotateur user, RedirectAttributes redirectAttributes) {
        boolean isUpdate = false;
        try {
            isUpdate = user.getId() != null;

            if (!isUpdate) {
                // Generate random password for new users
                String rawPassword = passwordService.generateRandomPassword(12);

                // Set the password in encoded form
                user.setPassword(rawPassword); // This will be encoded by UserServiceImpl

                Role annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
                if (annotateurRole == null) {
                    throw new IllegalStateException("Annotateur role not found in the database");
                }
                user.setRole(annotateurRole);
                user.setDeleted(false);

                // Save the user first to get the ID
                annotateurService.save(user);

                // Now send the welcome email with login details
                String email = user.getEmail();
                if (email != null) {
                    passwordService.sendWelcomeEmail(email, user.getLogin(), rawPassword);
                }
            } else {
                // For updates, retrieve existing user to preserve role and other fields
                Annotateur existingAnnotateur = annotateurService.findAnnotateurById(user.getId());
                if (existingAnnotateur == null) {
                    throw new IllegalStateException("Annotateur not found for update");
                }

                // Preserve role and deleted status
                user.setRole(existingAnnotateur.getRole());
                user.setDeleted(existingAnnotateur.isDeleted());
                annotateurService.save(user);
            }

            String successMessage = isUpdate ? "Annotateur updated successfully" : "Annotateur added successfully";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/annotateurs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error " + (isUpdate ? "updating" : "adding") + " annotateur: " + e.getMessage());
            if (isUpdate) {
                return "redirect:/admin/annotateurs/update/" + user.getId();
            } else {
                return "redirect:/admin/annotateurs/add";
            }
        }
    }

    @GetMapping("/annotateurs/details/{id}")
    public String viewAnnotateurDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
            model.addAttribute("currentUserName", currentUserName);

            Annotateur annotateur = annotateurService.findAnnotateurById(id);
            if (annotateur == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Annotateur not found");
                return "redirect:/admin/annotateurs";
            }

            // Get annotator's tasks and statistics
            List<Tache> tasks = tacheService.findAllTasksByAnnotateurId(id);
            int totalTasks = tasks.size();

            // Calculate task status based on annotations and deadline
            Date currentDate = new Date();
            int completedTasks = 0;
            int activeTasks = 0;
            int expiredTasks = 0;

            for (Tache task : tasks) {
                // Count annotations for this task by this annotator
                int annotationsCount = annotationService.findAllAnnotationsByUser(annotateur).size();
                int totalCouples = task.getCouples().size();

                // Check if task is expired
                boolean isExpired = task.getDateLimite() != null && task.getDateLimite().before(currentDate);

                if (isExpired) {
                    expiredTasks++;
                } else if (annotationsCount >= totalCouples && totalCouples > 0) {
                    completedTasks++;
                } else {
                    activeTasks++;
                }
            }

            // Get total annotations by this annotator
            int totalAnnotations = annotationService.findAllAnnotationsByUser(annotateur).size();

            // Get last activity
            ProgressTache latestProgress = taskProgressService.getLastAnnotationByUser(annotateur);

            model.addAttribute("annotateur", annotateur);
            model.addAttribute("tasks", tasks);
            model.addAttribute("totalTasks", totalTasks);
            model.addAttribute("completedTasks", completedTasks);
            model.addAttribute("activeTasks", activeTasks);
            model.addAttribute("expiredTasks", expiredTasks);
            model.addAttribute("totalAnnotations", totalAnnotations);
            model.addAttribute("latestProgress", latestProgress);

            return "admin/annotateur_management/annotateur_details";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading annotateur details: " + e.getMessage());
            return "redirect:/admin/annotateurs";
        }
    }

    @GetMapping("/annotateurs/delete/{id}")
    public String deleteAnnotateur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Annotateur annotateur = annotateurService.findAnnotateurById(id);
        if (annotateur == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Annotateur not found");
            return "redirect:/admin/annotateurs";
        }
        annotateurService.deleteLogically(id);
        redirectAttributes.addFlashAttribute("successMessage", "Annotateur deleted successfully");
        return "redirect:/admin/annotateurs";
    }


}