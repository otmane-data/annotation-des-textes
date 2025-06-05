package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Annotateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotateurRepository extends JpaRepository<Annotateur, Long> {
    List<Annotateur> findAllByDeleted(boolean b);

    Page<Annotateur> findAllByDeletedFalse(PageRequest of);
    

}