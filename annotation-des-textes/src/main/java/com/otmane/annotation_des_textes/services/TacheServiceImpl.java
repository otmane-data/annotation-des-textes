package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Annotation;
import com.otmane.annotation_des_textes.entities.Tache;
import com.otmane.annotation_des_textes.repositories.AnnotationRepository;
import com.otmane.annotation_des_textes.repositories.TacheRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TacheServiceImpl implements TacheService {

    private final TacheRepository tacheRepository;
    private final AnnotateurService annotateurService;
    private final AnnotationRepository annotationRepository;
    public TacheServiceImpl(TacheRepository tacheRepository, AnnotateurService annotateurService, AnnotationRepository annotationRepository) {
        this.tacheRepository = tacheRepository;
        this.annotateurService = annotateurService;
        this.annotationRepository = annotationRepository;
    }


    @Override
    public Tache findTaskById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        return tacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));
    }
    @Override
    public List<Tache> findAllTasks() {
        return tacheRepository.findAll();
    }

    @Override
    public List<Tache> findAllTasksByAnnotateurId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Annotateur ID cannot be null");
        }
        Annotateur annotateur = annotateurService.findAnnotateurById(id);
        return tacheRepository.findByAnnotateur(annotateur);
    }
    @Override
    public String getSelectedClassId(Long taskId, Long coupleId, Long annotateurId) {
        if (taskId == null || coupleId == null || annotateurId == null) {
            throw new IllegalArgumentException("Task ID, Couple ID, and Annotateur ID cannot be null");
        }
        
        // Find the annotation for this task, couple, and annotateur
        Optional<Annotation> existingAnnotation = annotationRepository.findByAnnotateurIdAndCoupleTextId(
                annotateurId, coupleId);

        // If an annotation exists, return the chosen class string
        return existingAnnotation.map(Annotation::getChosenClass).orElse(null);
    }

    @Override
    public long countActiveTasks(){
        return tacheRepository.count();
    }
    @Override
    public Long countAssignedCouples(Annotateur annotateur) {
        if (annotateur == null) {
            throw new IllegalArgumentException("Annotateur cannot be null");
        }
        try {
            // Get tasks with couples eagerly loaded to prevent LazyInitializationException
            List<Tache> tasks = tacheRepository.findByAnnotateur(annotateur);
            if (tasks.isEmpty()) {
                return 0L;
            }
            return tasks.stream()
                    .filter(tache -> tache.getCouples() != null)
                    .mapToLong(tache -> tache.getCouples().size())
                    .sum();
        } catch (Exception e) {
            throw new RuntimeException("Error counting assigned couples: " + e.getMessage(), e);
        }
    }


    @Override
    public List<Tache> getValidTasksForAnnotateur(Long annotateurId) {
        if (annotateurId == null) {
            throw new IllegalArgumentException("Annotateur ID cannot be null");
        }
        Date currentDate = new Date();
        System.out.println("DEBUG: Getting valid tasks for annotator ID: " + annotateurId);
        System.out.println("DEBUG: Current date: " + currentDate);

        List<Tache> allTasks = tacheRepository.findAll();
        System.out.println("DEBUG: Total tasks in database: " + allTasks.size());

        List<Tache> userTasks = allTasks.stream()
                .filter(tache -> tache.getAnnotateur() != null &&
                        tache.getAnnotateur().getId().equals(annotateurId))
                .collect(java.util.stream.Collectors.toList());
        System.out.println("DEBUG: Tasks for user " + annotateurId + ": " + userTasks.size());

        // Filter tasks where date_limit is after or equal to current date (include today)
        List<Tache> validTasks = userTasks.stream()
                .filter(tache -> tache.getDateLimite() != null &&
                        (tache.getDateLimite().after(currentDate) || isSameDay(tache.getDateLimite(), currentDate)))
                .collect(java.util.stream.Collectors.toList());

        System.out.println("DEBUG: Valid (non-expired) tasks: " + validTasks.size());
        for (Tache task : validTasks) {
            System.out.println("DEBUG: Task ID " + task.getId() + ", deadline: " + task.getDateLimite());
        }

        return validTasks;
    }

    // Helper method to check if two dates are on the same day
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    @Override
    public List<Tache> findTasksByDatasetId(Long datasetId) {
        if (datasetId == null) {
            throw new IllegalArgumentException("Dataset ID cannot be null");
        }
        return tacheRepository.findAll().stream()
            .filter(task -> task.getDataset() != null && task.getDataset().getId().equals(datasetId))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Tache save(Tache tache) {
        if (tache == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        return tacheRepository.save(tache);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return tacheRepository.existsById(id);
    }

    @Override
    public long count() {
        return tacheRepository.count();
    }

}
