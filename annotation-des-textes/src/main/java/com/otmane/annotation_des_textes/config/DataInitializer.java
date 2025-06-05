package com.otmane.annotation_des_textes.config;

import com.otmane.annotation_des_textes.entities.Administrateur;
import com.otmane.annotation_des_textes.entities.Role;
import com.otmane.annotation_des_textes.entities.RoleType;
import com.otmane.annotation_des_textes.repositories.AdministrateurRepository;
import com.otmane.annotation_des_textes.repositories.RoleRepository;
import com.otmane.annotation_des_textes.services.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise les données de base pour l'application
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdministratorService administratorService;
    @Autowired
    public DataInitializer(RoleRepository roleRepository, 
                          AdministrateurRepository administrateurRepository,
                          PasswordEncoder passwordEncoder,
                          AdministratorService administratorService) {
        this.roleRepository = roleRepository;
        this.administrateurRepository = administrateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.administratorService = administratorService;
    }

    @Override
    public void run(String... args) {
        // Initialiser les rôles s'ils n'existent pas
        initRoles();
        
        // Initialiser un compte administrateur par défaut
        initDefaultAdmin();
    }

    private void initRoles() {
        // Créer le rôle ADMIN_ROLE s'il n'existe pas
        if (roleRepository.findByRole(RoleType.ADMIN_ROLE) == null) {
            Role adminRole = new Role();
            adminRole.setRole(RoleType.ADMIN_ROLE);
            roleRepository.save(adminRole);
            System.out.println("Rôle ADMIN_ROLE créé");
        }
        
        // Créer le rôle USER_ROLE s'il n'existe pas
        if (roleRepository.findByRole(RoleType.USER_ROLE) == null) {
            Role userRole = new Role();
            userRole.setRole(RoleType.USER_ROLE);
            roleRepository.save(userRole);
            System.out.println("Rôle USER_ROLE créé");
        }
    }

    private void initDefaultAdmin() {
        // Vérifier si un administrateur existe déjà
        if (administrateurRepository.count() == 0) {
            // Créer un administrateur par défaut
            Administrateur admin = new Administrateur();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setLogin("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@example.com");
            admin.setDeleted(false);
            
            // Assigner le rôle ADMIN_ROLE
            Role adminRole = roleRepository.findByRole(RoleType.ADMIN_ROLE);
            admin.setRole(adminRole);
            
            // Sauvegarder l'administrateur
            administratorService.save(admin);
            
            System.out.println("Administrateur par défaut créé avec login: admin et mot de passe: admin");
        }
    }
}