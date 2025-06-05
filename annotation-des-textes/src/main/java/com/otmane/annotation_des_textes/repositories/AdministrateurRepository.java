package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {
    List<Administrateur> findAllByDeleted(boolean isDeleted);
    Administrateur findByLogin(String login);
}
