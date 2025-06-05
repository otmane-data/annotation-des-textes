package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.ClassPossible;
import com.otmane.annotation_des_textes.entities.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassPossibleRepository extends JpaRepository<ClassPossible, Long> {
    List<ClassPossible> findByDataset(Dataset dataset);
}