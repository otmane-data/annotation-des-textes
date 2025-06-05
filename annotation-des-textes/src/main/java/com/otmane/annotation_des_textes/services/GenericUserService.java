package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Utilisateur;
import com.otmane.annotation_des_textes.repositories.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Abstract base service for all user types providing common functionality
 */
public abstract class GenericUserService {
    protected final UtilisateurRepository utilisateurRepository;
    protected final PasswordEncoder passwordEncoder;

    public GenericUserService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a user by ID, regardless of their specific type
     */
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur not found"));
    }

    /**
     * Finds a user by login, regardless of their specific type
     */
    public Utilisateur findByLogin(String login) {
        return utilisateurRepository.findByLogin(login);
    }

    /**
     * Abstract method for saving a user, to be implemented by specific user services
     */
    public abstract Utilisateur save(Utilisateur utilisateur);

    /**
     * Common method for logical deletion of users
     */
    @Transactional
    public void deleteLogically(Long id) {
        Utilisateur utilisateur = findById(id);
        utilisateur.setDeleted(true);
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Helper method for encoding passwords when needed
     */
    protected void encodePasswordIfNeeded(Utilisateur utilisateur, Utilisateur existingUtilisateur) {
        if (utilisateur.getPassword() != null && !utilisateur.getPassword().isEmpty() &&
                !utilisateur.getPassword().equals(existingUtilisateur.getPassword())) {
            utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));
        } else if (existingUtilisateur != null) {
            utilisateur.setPassword(existingUtilisateur.getPassword());
        }
    }

    /**
     * Copy basic user properties from source to target
     */
    protected void copyUserProperties(Utilisateur source, Utilisateur target) {
        target.setNom(source.getNom());
        target.setPrenom(source.getPrenom());
        target.setLogin(source.getLogin());
        target.setPassword(source.getPassword());
        target.setEmail(source.getEmail());
        target.setRole(source.getRole());
        target.setDeleted(source.isDeleted());
        target.setPassword(source.getPassword());
        target.setEmail(source.getEmail());
        target.setRole(source.getRole());
        target.setDeleted(source.isDeleted());
    }
}