package com.training.starter.common;

import com.training.starter.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafePageRequestTest {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "patientId", "patient.id",
            "id", "id"
    );

    @Test
    void of_validRequest_mapsSortFieldAndAddsStableTieBreaker() {
        var pageable = SafePageRequest.of(1, 50, "patientId", "DESC", SORT_FIELDS, "createdAt", Sort.Direction.ASC);

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().toList())
                .extracting(Sort.Order::getProperty)
                .containsExactly("patient.id", "id");
        assertThat(pageable.getSort().getOrderFor("patient.id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void of_idSort_doesNotDuplicateTieBreaker() {
        var pageable = SafePageRequest.of(0, 20, "id", "ASC", SORT_FIELDS, "createdAt", Sort.Direction.ASC);

        assertThat(pageable.getSort().toList())
                .extracting(Sort.Order::getProperty)
                .containsExactly("id");
    }

    @Test
    void of_invalidPage_throwsBadRequestException() {
        assertThatThrownBy(() -> SafePageRequest.of(-1, 20, "createdAt", "ASC", SORT_FIELDS, "createdAt", Sort.Direction.ASC))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void of_invalidSize_throwsBadRequestException() {
        assertThatThrownBy(() -> SafePageRequest.of(0, 101, "createdAt", "ASC", SORT_FIELDS, "createdAt", Sort.Direction.ASC))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void of_invalidSortField_throwsBadRequestException() {
        assertThatThrownBy(() -> SafePageRequest.of(0, 20, "password", "ASC", SORT_FIELDS, "createdAt", Sort.Direction.ASC))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void of_invalidSortDirection_throwsBadRequestException() {
        assertThatThrownBy(() -> SafePageRequest.of(0, 20, "createdAt", "SIDEWAYS", SORT_FIELDS, "createdAt", Sort.Direction.ASC))
                .isInstanceOf(BadRequestException.class);
    }
}
