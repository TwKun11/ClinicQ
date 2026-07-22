package com.training.starter.repository;

import com.training.starter.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
