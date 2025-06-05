package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import com.otmane.annotation_des_textes.repositories.AnnotateurRepository;
import com.otmane.annotation_des_textes.repositories.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for Annotateur-specific operations, extending the GenericUserService
 */
@Service
public class AnnotateurService extends GenericUserService {
    private final AnnotateurRepository annotateurRepository;

    @Autowired
    public AnnotateurService(UtilisateurRepository utilisateurRepository,
                             AnnotateurRepository annotateurRepository,
                             PasswordEncoder passwordEncoder) {
        super(utilisateurRepository, passwordEncoder);
        this.annotateurRepository = annotateurRepository;
    }

    /**
     * Find all active (non-deleted) annotateurs
     */
    public List<Annotateur> findAllActive() {
        return annotateurRepository.findAllByDeleted(false);
    }

    public Page<Annotateur> findAllActive(int page, int size) {
        return annotateurRepository.findAllByDeletedFalse(PageRequest.of(page, size));
    }

    public List<Annotateur> findAllByIds(List<Long> ids) {
        return annotateurRepository.findAllById(ids);
    }

    /**
     * Find annotateur by ID with type safety
     */
    public Annotateur findAnnotateurById(Long id) {
        return annotateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotateur not found with ID: " + id));
    }

    /**
     * Implementation of the abstract save method for annotateurs
     */
    @Override
    @Transactional
    public Utilisateur save(Utilisateur utilisateur) {
        // Handle the case when it's a new user but not an Annotateur instance
        if (!(utilisateur instanceof Annotateur) && utilisateur.getId() == null) {
            Annotateur annotateur = new Annotateur();
            copyUserProperties(utilisateur, annotateur);
            utilisateur = annotateur;
        }

        // If it's an update operation
        if (utilisateur.getId() != null) {
            // Find the existing annotateur
            Utilisateur finalUser = utilisateur;
            Annotateur existingAnnotateur = annotateurRepository.findById(utilisateur.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Annotateur not found with ID: " + finalUser.getId()));

            // Update basic properties
            existingAnnotateur.setNom(utilisateur.getNom());
            existingAnnotateur.setPrenom(utilisateur.getPrenom());
            existingAnnotateur.setLogin(utilisateur.getLogin());
            existingAnnotateur.setRole(utilisateur.getRole());
            existingAnnotateur.setEmail(utilisateur.getEmail());
            existingAnnotateur.setDeleted(utilisateur.isDeleted());

            // Handle password encoding if needed
            encodePasswordIfNeeded(existingAnnotateur, utilisateur);

            return annotateurRepository.save(existingAnnotateur);
        } else {
            // For new users, encode the password

            utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));
            return annotateurRepository.save((Annotateur) utilisateur);
        }
    }

    public long countActiveAnnotateurs() {
        return annotateurRepository.count();
    }

    /**
     * Find annotateur by utilisateur ID
     */
    public Annotateur findAnnotateurByUtilisateurId(Long utilisateurId) {
        return annotateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new EntityNotFoundException("Annotateur not found with utilisateur ID: " + utilisateurId));
    }
}