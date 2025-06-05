package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.services.AnnotateurService;
import com.otmane.annotation_des_textes.services.AssignTaskToAnnotator;
import com.otmane.annotation_des_textes.services.DatasetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class TaskController {

    private final DatasetService datasetService;
    private final AssignTaskToAnnotator assignTaskToAnnotator;
    private final AnnotateurService annotateurService;

    public TaskController(DatasetService datasetService, AssignTaskToAnnotator assignTaskToAnnotator, AnnotateurService annotateurService) {
        this.datasetService = datasetService;
        this.assignTaskToAnnotator = assignTaskToAnnotator;
        this.annotateurService = annotateurService;
    }


    @PostMapping("/datasets/{id}/assign")
    public String processAssignment(
            @PathVariable Long id,
            @RequestParam(value = "selectedAnnotateurs", required = false) List<Long> annotatorIds,
            @RequestParam("deadline") @DateTimeFormat(pattern = "yyyy-MM-dd") Date deadline,
            RedirectAttributes redirectAttributes) {

        System.out.println("DEBUG: Processing assignment for dataset " + id);
        System.out.println("DEBUG: Selected annotators: " + annotatorIds);
        System.out.println("DEBUG: Deadline: " + deadline);

        try {
            // Check if dataset exists
            Dataset dataset = datasetService.findDatasetById(id);
            if (dataset == null) {
                System.out.println("DEBUG: Dataset not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Dataset not found");
                return "redirect:/admin/datasets";
            }

            // Check if annotators are selected (FIRST check for null/empty)
            if (annotatorIds == null || annotatorIds.isEmpty()) {
                System.out.println("DEBUG: No annotators selected");
                redirectAttributes.addFlashAttribute("error", "Please select at least one annotator");
                return "redirect:/admin/datasets/" + id + "/assign_annotator";
            }

            // Check minimum annotators requirement (AFTER null check)
            if (annotatorIds.size() < 3) {
                System.out.println("DEBUG: Not enough annotators selected: " + annotatorIds.size());
                redirectAttributes.addFlashAttribute("error", "Au moins 3 annotateurs actifs sont requis pour l'annotation.");
                return "redirect:/admin/datasets/" + id + "/assign_annotator";
            }

            // Fetch the checked annotators
            List<Annotateur> annotateursList = annotateurService.findAllByIds(annotatorIds);
            System.out.println("DEBUG: Found " + annotateursList.size() + " annotators in database");

            if (annotateursList.size() != annotatorIds.size()) {
                System.out.println("DEBUG: Some annotators not found in database");
                redirectAttributes.addFlashAttribute("error", "Some selected annotators were not found");
                return "redirect:/admin/datasets/" + id + "/assign_annotator";
            }

            // Perform the assignment
            assignTaskToAnnotator.assignTaskToAnnotator(annotateursList, dataset, deadline);
            System.out.println("DEBUG: Assignment completed successfully");
            redirectAttributes.addFlashAttribute("success", "Annotators successfully assigned to dataset");

        } catch (Exception e) {
            System.out.println("DEBUG: Error during assignment: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error assigning annotators: " + e.getMessage());
            return "redirect:/admin/datasets/" + id + "/assign_annotator";
        }

        return "redirect:/admin/datasets/details/" + id;
    }

}