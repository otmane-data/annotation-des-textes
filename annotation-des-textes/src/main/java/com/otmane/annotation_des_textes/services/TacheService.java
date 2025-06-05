package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Tache;

import java.util.List;

public interface TacheService {
    /**
     * Find a task by its ID
     * @param id The ID of the task to find
     * @return The task if found
     * @throws IllegalArgumentException if task not found
     */
    Tache findTaskById(Long id);

    /**
     * Retrieve all tasks in the system
     * @return List of all tasks
     */
    List<Tache> findAllTasks();

    /**
     * Find all tasks assigned to a specific annotator
     * @param id The ID of the annotator
     * @return List of tasks assigned to the annotator
     */
    List<Tache> findAllTasksByAnnotateurId(Long id);

    /**
     * Get the selected class ID for a specific annotation task
     * @param taskId The ID of the task
     * @param coupleId The ID of the text couple
     * @param annotateurId The ID of the annotator
     * @return The selected class ID or null if not found
     */
    String getSelectedClassId(Long taskId, Long coupleId, Long annotateurId);

    /**
     * Count the number of active tasks in the system
     * @return The number of active tasks
     */
    long countActiveTasks();

    /**
     * Count the number of text couples assigned to an annotator
     * @param annotateur The annotator
     * @return The number of assigned couples
     */
    Long countAssignedCouples(Annotateur annotateur);

    /**
     * Get all valid (not expired) tasks for an annotator
     * @param annotateurId The ID of the annotator
     * @return List of valid tasks
     */
    List<Tache> getValidTasksForAnnotateur(Long annotateurId);

    /**
     * Find tasks by dataset ID
     * @param datasetId The dataset ID
     * @return List of tasks for the dataset
     */
    List<Tache> findTasksByDatasetId(Long datasetId);

    /**
     * Save a task
     * @param tache The task to save
     * @return The saved task
     */
    Tache save(Tache tache);

    /**
     * Check if a task exists by ID
     * @param id The task ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Count total tasks
     * @return Total number of tasks
     */
    long count();
}