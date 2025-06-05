package com.otmane.annotation_des_textes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;

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
} 