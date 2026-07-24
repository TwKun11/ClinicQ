package com.training.starter.repository;

import com.training.starter.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByUserId(Long userId);

    Page<Doctor> findBySpecialtyContainingIgnoreCase(String specialty, Pageable pageable);
}
