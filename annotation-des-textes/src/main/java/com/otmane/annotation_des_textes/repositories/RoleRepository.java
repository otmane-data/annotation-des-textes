package com.otmane.annotation_des_textes.repositories;

import com.otmane.annotation_des_textes.entities.Administrateur;
import com.otmane.annotation_des_textes.entities.Role;
import com.otmane.annotation_des_textes.entities.RoleType;
import com.otmane.annotation_des_textes.entities.Tache;
import org.hibernate.mapping.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByRole(RoleType role);

}
