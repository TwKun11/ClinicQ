package com.training.starter.repository.specification;

import com.training.starter.entity.Patient;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + escapeLike(search.trim().toLowerCase(Locale.ROOT)) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern, '\\'),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern, '\\'),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern, '\\'));
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
