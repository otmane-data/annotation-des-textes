package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.Dataset;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.repositories.TacheRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Transactional
public class AssignTaskToAnnotator {

    private final CoupleTextService coupleTextService;
    private final TacheRepository tacheRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AssignTaskToAnnotator(CoupleTextService coupleTextService, TacheRepository tacheRepository) {
        this.coupleTextService = coupleTextService;
        this.tacheRepository = tacheRepository;
    }


    public void assignTaskToAnnotator(List<Annotateur> annotateurList, Dataset dataset, Date deadline) {
        try {
            System.out.println("DEBUG: Starting task assignment for dataset: " + dataset.getName());
            System.out.println("DEBUG: Number of annotators: " + annotateurList.size());
            System.out.println("DEBUG: Deadline: " + deadline);

            // Validation des paramètres
            if (annotateurList == null || annotateurList.isEmpty()) {
                throw new IllegalArgumentException("Annotator list cannot be null or empty");
            }
            if (dataset == null) {
                throw new IllegalArgumentException("Dataset cannot be null");
            }
            if (deadline == null) {
                throw new IllegalArgumentException("Deadline cannot be null");
            }

            // Récupération des paires de texte du dataset
            Long datasetId = dataset.getId();
            List<CoupleText> coupleTextList = coupleTextService.findAllCoupleTextsByDatasetId(datasetId);
            System.out.println("DEBUG: Found " + coupleTextList.size() + " text pairs in dataset");

            if (coupleTextList.isEmpty()) {
                throw new IllegalArgumentException("No text pairs found for this dataset.");
            }

        // Étape 1 : Duplication des paires (3 fois chacune)
        List<CoupleText> duplicatedPairs = new ArrayList<>();
        for (CoupleText couple : coupleTextList) {
            for (int i = 0; i < 3; i++) {  // 3 annotations par paire
                duplicatedPairs.add(new CoupleText(couple));  // Constructeur de copie ou méthode clone()
            }
        }

        // Étape 2 : Mélanger les paires dupliquées
        Collections.shuffle(duplicatedPairs);

        // Étape 3 : Assignation round-robin aux annotateurs
        int annotatorCount = annotateurList.size();
        Map<Annotateur, List<CoupleText>> taskMap = new HashMap<>();

        // Initialisation des listes de tâches
        for (Annotateur a : annotateurList) {
            taskMap.put(a, new ArrayList<>());
        }

        // Distribution round-robin
        for (int i = 0; i < duplicatedPairs.size(); i++) {
            Annotateur annotator = annotateurList.get(i % annotatorCount);
            CoupleText pair = duplicatedPairs.get(i);

            // Éviter qu'un annotateur reçoive la même paire originale plusieurs fois
            if (!hasAnnotatorAlreadyReceivedOriginalPair(annotator, pair, coupleTextList)) {
                taskMap.get(annotator).add(pair);
            } else {
                // Si déjà attribué, passer à l'annotateur suivant (simple fallback)
                int nextIndex = (i + 1) % annotatorCount;
                taskMap.get(annotateurList.get(nextIndex)).add(pair);
            }
        }

        // Étape 4 : Création des tâches dans la base de données
        System.out.println("DEBUG: Creating tasks in database...");
        int taskCount = 0;
        for (Map.Entry<Annotateur, List<CoupleText>> entry : taskMap.entrySet()) {
            Annotateur annotator = entry.getKey();
            List<CoupleText> tasks = entry.getValue();

            if (!tasks.isEmpty()) {
                System.out.println("DEBUG: Creating task for annotator: " + annotator.getNom() + " with " + tasks.size() + " couples");

                try {
                    System.out.println("DEBUG: About to create task entity...");
                    Tache tache = new Tache();
                    System.out.println("DEBUG: Task entity created");

                    System.out.println("DEBUG: Setting annotator: " + annotator.getId() + " - " + annotator.getNom());
                    tache.setAnnotateur(annotator);

                    System.out.println("DEBUG: Setting dataset: " + dataset.getId() + " - " + dataset.getName());
                    tache.setDataset(dataset);

                    System.out.println("DEBUG: Setting deadline: " + deadline);
                    tache.setDateLimite(deadline);

                    System.out.println("DEBUG: About to save task to repository...");
                    // Sauvegarder d'abord la tâche sans les couples
                    Tache savedTache = tacheRepository.save(tache);
                    System.out.println("DEBUG: Task saved with ID: " + savedTache.getId());

                    // Vérifier que la tâche existe en base
                    System.out.println("DEBUG: Verifying task exists in database...");
                    boolean exists = tacheRepository.existsById(savedTache.getId());
                    System.out.println("DEBUG: Task exists in DB: " + exists);

                    System.out.println("DEBUG: Adding " + tasks.size() + " couples to task...");
                    // Puis ajouter les couples
                    savedTache.setCouples(tasks);

                    System.out.println("DEBUG: Saving task with couples...");
                    tacheRepository.save(savedTache);

                    System.out.println("DEBUG: Flushing entity manager...");
                    // Forcer la persistence immédiate
                    entityManager.flush();

                    System.out.println("DEBUG: Verifying final task state...");
                    Tache finalTask = tacheRepository.findById(savedTache.getId()).orElse(null);
                    if (finalTask != null) {
                        System.out.println("DEBUG: Final task found with " + finalTask.getCouples().size() + " couples");
                    } else {
                        System.out.println("ERROR: Final task not found in database!");
                    }

                    taskCount++;
                    System.out.println("DEBUG: Successfully created task ID " + savedTache.getId() + " for annotator: " + annotator.getNom());

                } catch (Exception e) {
                    System.err.println("ERROR: Failed to save task for annotator " + annotator.getNom() + ": " + e.getMessage());
                    System.err.println("ERROR: Exception type: " + e.getClass().getSimpleName());
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        System.out.println("DEBUG: Successfully created " + taskCount + " tasks");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to assign tasks - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to assign tasks: " + e.getMessage(), e);
        }
    }

    // Méthode utilitaire pour éviter la répétition d'une paire originale pour un annotateur
    private boolean hasAnnotatorAlreadyReceivedOriginalPair(Annotateur annotator, CoupleText duplicatedPair, List<CoupleText> originalPairs) {
        // Vérifier que l'ID original n'est pas null
        if (duplicatedPair.getOriginalId() == null) {
            return false;
        }

        try {
            // Recherche de la paire originale correspondante
            boolean hasOriginalPair = originalPairs.stream()
                    .anyMatch(original -> original.getId().equals(duplicatedPair.getOriginalId()));

            if (!hasOriginalPair) {
                return false;
            }

            // Utiliser le repository pour éviter LazyInitializationException
            List<Tache> annotatorTaches = tacheRepository.findByAnnotateur(annotator);
            return annotatorTaches.stream()
                    .flatMap(t -> t.getCouples().stream())
                    .anyMatch(c -> c.getOriginalId() != null && c.getOriginalId().equals(duplicatedPair.getOriginalId()));

        } catch (Exception e) {
            // En cas d'erreur, on considère que l'annotateur n'a pas reçu la paire
            System.err.println("Error checking if annotator already received pair: " + e.getMessage());
            return false;
        }
    }

    public void unassignAnnotator(Long datasetId, Long annotatorId) {
        List<Tache> taches = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotatorId);

        if (taches.isEmpty()) {
            throw new IllegalArgumentException("No task found for this annotator and dataset.");
        }
        for (Tache tache : taches) {
            tache.setAnnotateur(null);
            tacheRepository.save(tache);
        }
    }

}