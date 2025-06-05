package com.otmane.annotation_des_textes.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nli_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NliPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "couple_id")
    private CoupleText coupleText;
    
    @Column(name = "label", nullable = false)
    private String label;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "prediction_date")
    private java.time.LocalDateTime predictionDate;
    
    /**
     * Méthode pour compatibilité avec l'ancien code
     */
    public Double getScore() {
        return this.confidenceScore;
    }
    
    /**
     * Méthode pour compatibilité avec l'ancien code
     */
    public void setCoupleTextId(Long id) {
        if (this.coupleText == null) {
            this.coupleText = new CoupleText();
        }
        this.coupleText.setId(id);
    }
    
    @PrePersist
    protected void onCreate() {
        predictionDate = java.time.LocalDateTime.now();
    }
} 