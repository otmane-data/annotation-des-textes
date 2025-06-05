package com.otmane.annotation_des_textes.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spam_detection_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpamDetectionResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "annotateur_id", nullable = false)
    private Annotateur annotateur;
    
    @Column(name = "detection_date", nullable = false)
    private LocalDateTime detectionDate;
    
    @Column(name = "spam_score")
    private Double spamScore;
    
    @Column(name = "is_flagged")
    private boolean flagged;
    
    @Column(name = "threshold_used")
    private Double thresholdUsed;
    
    @Column(length = 1000)
    private String details;
    
    @PrePersist
    protected void onCreate() {
        detectionDate = LocalDateTime.now();
    }
    
    /**
     * Méthode pour compatibilité avec l'ancien code
     */
    public void setMismatchRate(Double rate) {
        this.spamScore = rate;
    }
} 