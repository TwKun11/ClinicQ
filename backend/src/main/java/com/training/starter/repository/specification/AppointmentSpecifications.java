package com.training.starter.repository.specification;

import com.training.starter.entity.Appointment;
import com.training.starter.enums.AppointmentStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    public static Specification<Appointment> matchesFilters(
            LocalDate date,
            Long patientId,
            AppointmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("patient", JoinType.LEFT);
                root.fetch("doctor", JoinType.LEFT);
                root.fetch("slot", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (date != null) {
                predicates.add(criteriaBuilder.equal(root.get("appointmentDate"), date));
            }
            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
