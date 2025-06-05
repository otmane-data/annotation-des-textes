package com.otmane.annotation_des_textes.services;

import com.otmane.annotation_des_textes.entities.Administrateur;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import com.otmane.annotation_des_textes.repositories.AdministrateurRepository;
import com.otmane.annotation_des_textes.repositories.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Administrator-specific operations, extending the GenericUserService
 */
@Service
public class AdministratorService extends GenericUserService {
    private final AdministrateurRepository administrateurRepository;

    public AdministratorService(UtilisateurRepository utilisateurRepository,
                                AdministrateurRepository administrateurRepository,
                                PasswordEncoder passwordEncoder) {
        super(utilisateurRepository, passwordEncoder);
        this.administrateurRepository = administrateurRepository;
    }

    /**
     * Find all active (non-deleted) administrators
     */
    public List<Administrateur> findAllActive() {
        return administrateurRepository.findAllByDeleted(false);
    }

    /**
     * Find administrator by ID
     */
    public Administrateur findAdministratorById(Long id) {
        return administrateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur not found"));
    }

    /**
     * Implementation of the abstract save method for administrators
     */
    @Override
    @Transactional
    public Utilisateur save(Utilisateur utilisateur) {
        if (!(utilisateur instanceof Administrateur) && utilisateur.getId() == null) {
            // If it's a plain User object for a new administrator, convert it
            Administrateur administrateur = new Administrateur();
            copyUserProperties(utilisateur, administrateur);
            utilisateur = administrateur;
        }

        // If it's an update operation
        if (utilisateur.getId() != null) {
            Administrateur existingAdministrateur = administrateurRepository.findById(utilisateur.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Administrateur not found"));

            // Update basic properties
            existingAdministrateur.setNom(utilisateur.getNom());
            existingAdministrateur.setPrenom(utilisateur.getPrenom());
            existingAdministrateur.setLogin(utilisateur.getLogin());
            existingAdministrateur.setRole(utilisateur.getRole());
            existingAdministrateur.setDeleted(utilisateur.isDeleted());

            // Handle password
            encodePasswordIfNeeded(existingAdministrateur, utilisateur);

            return administrateurRepository.save(existingAdministrateur);
        } else {
            // For new users, encode the password
            utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));
            return administrateurRepository.save((Administrateur) utilisateur);
        }
    }
}