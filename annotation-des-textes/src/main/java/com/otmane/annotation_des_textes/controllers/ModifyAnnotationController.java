package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.Annotation;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.services.AnnotationService;
import com.otmane.annotation_des_textes.services.CoupleTextService;
import com.otmane.annotation_des_textes.services.TaskProgressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ModifyAnnotationController {

    private final AnnotationService annotationService;
    private final CoupleTextService coupleTextService;
    private final TaskProgressService taskProgressService;

    public ModifyAnnotationController(AnnotationService annotationService, CoupleTextService coupleTextService, TaskProgressService taskProgressService) {
        this.annotationService = annotationService;
        this.coupleTextService = coupleTextService;
        this.taskProgressService = taskProgressService;
    }

    @GetMapping("/tasks/{id}/annotate/{annotationId}/{coupleId}")
    public String viewAnnotationForm(@PathVariable Long annotationId,
                                     @PathVariable Long coupleId,
                                     @PathVariable Long id,
                                     Model model) {

        Annotation annotation = annotationService.findAnnotationById(annotationId);
        Dataset dataset = annotation.getCoupleText().getDataset();
        model.addAttribute("taskId", id);
        model.addAttribute("dataset", dataset);
        model.addAttribute("coupleId", coupleId);
        model.addAttribute("annotation", annotation);
        return "admin/tasks_management/modify_annotation_form";
    }
    @PostMapping("/update-annotation/{taskId}/{annotationId}")
    public String updateAnnotation(@PathVariable Long annotationId,
                                   @PathVariable Long taskId,
                                   @RequestParam String chosenClass,
                                   RedirectAttributes redirectAttributes){
        Annotation annotation = annotationService.findAnnotationById(annotationId);
        annotation.setChosenClass(chosenClass);
        annotationService.saveAnnotation(annotation.getChosenClass(), annotation.getCoupleText().getId(), annotation.getAnnotateur().getId());
        redirectAttributes.addFlashAttribute("successMessage", "Annotation updated successfully!");
        return "redirect:/admin/annotations/{taskId}";
    }
}