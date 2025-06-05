package com.otmane.annotation_des_textes.controllers;

import com.otmane.annotation_des_textes.entities.Annotation;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.entities.SpamDetectionResults;
import com.otmane.annotation_des_textes.repositories.SpamDetectionResultsRepository;
import com.otmane.annotation_des_textes.services.AnnotationService;
import com.otmane.annotation_des_textes.services.DatasetService;
import com.otmane.annotation_des_textes.services.InterAnnotatorAgreement;
import com.otmane.annotation_des_textes.services.UtilisateurService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdvancedStatisticsController {
    private final InterAnnotatorAgreement interAnnotatorAgreement;
    private final AnnotationService annotationService;
    private final UtilisateurService utilisateurService;
    private final DatasetService datasetService;
    private final SpamDetectionResultsRepository spamResultsRepository;


    public AdvancedStatisticsController(InterAnnotatorAgreement interAnnotatorAgreement,
                                        AnnotationService annotationService,
                                        UtilisateurService utilisateurService,
                                        DatasetService datasetService,
                                        SpamDetectionResultsRepository spamResultsRepository) {
        this.interAnnotatorAgreement = interAnnotatorAgreement;
        this.annotationService = annotationService;
        this.utilisateurService = utilisateurService;
        this.datasetService = datasetService;
        this.spamResultsRepository = spamResultsRepository;
    }

    @GetMapping("/advanced-stats")
    public String showAdvancedStatistics(Model model) {
        List<Dataset> datasets = datasetService.findAllDatasets();
        String currentUserName = StringUtils.capitalize(utilisateurService.getCurrentUserName());
        model.addAttribute("datasets", datasets);
        model.addAttribute("currentUserName", currentUserName);

        // Load spam detection results to display
        List<SpamDetectionResults> spamResults = spamResultsRepository.findAll();
        model.addAttribute("results", spamResults);

        // Count flagged annotators
        long flaggedCount = spamResults.stream().filter(SpamDetectionResults::isFlagged).count();
        model.addAttribute("flaggedCount", flaggedCount);

        return "admin/statistics_management/advanced_stats";
    }

    @PostMapping("/metrics-calculate")
    public String calculateMetrics(@RequestParam("datasetId") Long datasetId, RedirectAttributes redirectAttributes) {
        Dataset dataset = datasetService.findDatasetById(datasetId);

        List<Annotation> annotations = dataset.getCoupleTexts().stream()
                .flatMap(c -> c.getAnnotations().stream())
                .collect(Collectors.toList());

        double fleissKappa = interAnnotatorAgreement.calculateFleissKappa(annotations);
        double cohenKappa = interAnnotatorAgreement.calculateCohensKappa(annotations);
        double percentAgreement = interAnnotatorAgreement.calculatePercentAgreement(annotations);
        String agreementStatus = interAnnotatorAgreement.getAgreementStatus(fleissKappa);

        int totalItems = dataset.getCoupleTexts().size();
        String datasetName = dataset.getName();

        // Add flash attributes to survive redirect
        redirectAttributes.addFlashAttribute("fleissKappa", fleissKappa);
        redirectAttributes.addFlashAttribute("cohenKappa", cohenKappa);
        redirectAttributes.addFlashAttribute("percentAgreement", percentAgreement);
        redirectAttributes.addFlashAttribute("agreementStatus", agreementStatus);
        redirectAttributes.addFlashAttribute("totalItems", totalItems);
        redirectAttributes.addFlashAttribute("selectedDatasetName", datasetName);

        return "redirect:/admin/advanced-stats";
    }


}
