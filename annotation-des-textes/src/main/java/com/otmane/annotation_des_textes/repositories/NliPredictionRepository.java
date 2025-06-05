package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.CoupleText;
import com.otmane.annotation_des_textes.entities.NliPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NliPredictionRepository extends JpaRepository<NliPrediction, Long> {
    List<NliPrediction> findByCoupleText(CoupleText coupleText);
} 