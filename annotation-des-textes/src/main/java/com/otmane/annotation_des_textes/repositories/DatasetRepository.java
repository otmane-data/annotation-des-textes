package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository  extends JpaRepository<Dataset, Long> {
    Dataset findByName(String name);
}
