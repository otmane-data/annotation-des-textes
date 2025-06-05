package com.otmane.annotation_des_textes.services;

/**
 * Interface pour la gestion des mots de passe
 */
public interface PasswordService {

    /**
     * Génère un mot de passe aléatoire sécurisé
     * @param length la longueur du mot de passe
     * @return le mot de passe généré
     */
    String generateRandomPassword(int length);

    /**
     * Encode un mot de passe
     * @param rawPassword le mot de passe en clair
     * @return le mot de passe encodé
     */
    String encodePassword(String rawPassword);

    /**
     * Vérifie si un mot de passe est valide
     * @param rawPassword le mot de passe en clair
     * @param encodedPassword le mot de passe encodé
     * @return true si le mot de passe est valide
     */
    boolean isPasswordValid(String rawPassword, String encodedPassword);

    /**
     * Envoie un email de bienvenue avec les informations de connexion
     * @param email l'adresse email de l'utilisateur
     * @param username le nom d'utilisateur
     * @param password le mot de passe en clair
     */
    void sendWelcomeEmail(String email, String username, String password);

    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param email l'adresse email de l'utilisateur
     * @param username le nom d'utilisateur
     * @param newPassword le nouveau mot de passe en clair
     */
    void sendPasswordResetEmail(String email, String username, String newPassword);
}
