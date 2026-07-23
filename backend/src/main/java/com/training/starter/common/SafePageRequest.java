package com.training.starter.common;

import com.training.starter.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SafePageRequest {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private SafePageRequest() {
    }

    public static Pageable of(
            int page,
            int size,
            String sortBy,
            String sortDir,
            Map<String, String> allowedSortFields,
            String defaultSortBy,
            Sort.Direction defaultDirection) {
        if (page < 0) {
            throw new BadRequestException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BadRequestException("Size must be between 1 and " + MAX_SIZE);
        }

        String requestedSort = sortBy == null || sortBy.isBlank() ? defaultSortBy : sortBy;
        String sortProperty = allowedSortFields.get(requestedSort);
        if (sortProperty == null) {
            throw new BadRequestException("Invalid sort field: " + requestedSort);
        }

        Sort.Direction direction = resolveDirection(sortDir, defaultDirection);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(direction, sortProperty));
        if (!"id".equals(sortProperty)) {
            orders.add(new Sort.Order(direction, "id"));
        }

        return PageRequest.of(page, size, Sort.by(orders));
    }

    private static Sort.Direction resolveDirection(String sortDir, Sort.Direction defaultDirection) {
        if (sortDir == null || sortDir.isBlank()) {
            return defaultDirection;
        }
        try {
            return Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid sort direction: " + sortDir);
        }
    }
}
