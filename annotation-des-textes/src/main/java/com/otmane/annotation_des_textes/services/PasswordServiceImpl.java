package com.otmane.annotation_des_textes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final JavaMailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()-_=+";

    @Autowired
    public PasswordServiceImpl(JavaMailSender emailSender, PasswordEncoder passwordEncoder) {
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8; // Longueur minimale pour la sécurité
        }
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%&*";
        String allChars = upperChars + lowerChars + numbers + specialChars;

        StringBuilder password = new StringBuilder(length);
        // Assurer au moins un caractère de chaque type
        password.append(upperChars.charAt(secureRandom.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(secureRandom.nextInt(lowerChars.length())));
        password.append(numbers.charAt(secureRandom.nextInt(numbers.length())));
        password.append(specialChars.charAt(secureRandom.nextInt(specialChars.length())));

        // Remplir le reste du mot de passe
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }

        // Mélanger le mot de passe
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean isPasswordValid(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Bienvenue sur notre plateforme");
        message.setText("Bonjour " + username + ",\n\n" +
                "Votre compte a été créé avec succès.\n" +
                "Voici vos identifiants de connexion:\n" +
                "Nom d'utilisateur: " + username + "\n" +
                "Mot de passe: " + password + "\n\n" +
                "Pour des raisons de sécurité, nous vous recommandons de changer votre mot de passe après votre première connexion.\n\n" +
                "Cordialement,\n" +
                "L'équipe de support");

        try {
            emailSender.send(message);
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer l'application
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String username, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour " + username + ",\n\n" +
                "Votre mot de passe a été réinitialisé.\n\n" +
                "Voici vos nouvelles informations de connexion :\n" +
                "Identifiant : " + username + "\n" +
                "Nouveau mot de passe : " + newPassword + "\n\n" +
                "Nous vous recommandons de changer votre mot de passe après vous être connecté.\n\n" +
                "Cordialement,\n" +
                "L'équipe d'administration");
        
        try {
            emailSender.send(message);
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer l'application
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }
}
