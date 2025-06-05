package com.otmane.annotation_des_textes.services;
import java.util.ArrayList;
import java.util.List;

import com.otmane.annotation_des_textes.entities.Annotateur;
import com.otmane.annotation_des_textes.entities.Utilisateur;
import com.otmane.annotation_des_textes.entities.RoleType;
import com.otmane.annotation_des_textes.repositories.AnnotateurRepository;
import com.otmane.annotation_des_textes.repositories.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final AnnotateurRepository annotateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, AnnotateurRepository annotateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.annotateurRepository = annotateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(username);
        if (utilisateur == null) {
            throw new UsernameNotFoundException("Utilisateur not found with username: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().getRole().name()));

        return org.springframework.security.core.userdetails.User
                .withUsername(utilisateur.getLogin())
                .password(utilisateur.getPassword())
                .authorities(authorities)
                .build();
    }
    
    public Utilisateur getCurrentUtilisateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String)) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            return utilisateurRepository.findByLogin(username);
        }
        return null;
    }
    
    public Long getCurrentUtilisateurId() {
        Utilisateur utilisateur = getCurrentUtilisateur();
        return utilisateur != null ? utilisateur.getId() : null;
    }
    
    public String getCurrentUserName() {
        Utilisateur utilisateur = getCurrentUtilisateur();
        return utilisateur != null ? utilisateur.getNom() : null;
    }
    

}
