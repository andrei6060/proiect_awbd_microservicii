package com.clinic.clinic.Entity.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Helper for turning raw paging/sorting request parameters into a validated,
 * clamped {@link Pageable}. The allow-list guards against sorting by arbitrary
 * (or non-existent) fields, and the resolved sort is echoed back so callers can
 * surface it in {@link PageResponse}.
 */
public final class PaginationUtil {

    private PaginationUtil() {
    }

    /**
     * Resolved paging request: the {@link Pageable} to hand to the repository
     * plus the effective sort field and direction (after validation/clamping).
     */
    public record PageQuery(Pageable pageable, String sortBy, String direction) {
    }

    public static PageQuery resolve(int page,
                                    int size,
                                    String sortBy,
                                    String direction,
                                    Set<String> allowedSortFields,
                                    String defaultSortField,
                                    int defaultPageSize,
                                    int maxPageSize) {
        // validate sort field against the allow-list, fall back to default if invalid
        String resolvedSortBy = (sortBy != null && allowedSortFields.contains(sortBy))
                ? sortBy
                : defaultSortField;

        // "desc" (case-insensitive) means descending, anything else ascending
        boolean descending = direction != null && direction.equalsIgnoreCase("desc");
        String resolvedDirection = descending ? "desc" : "asc";
        Sort.Direction sortDirection = descending ? Sort.Direction.DESC : Sort.Direction.ASC;

        // clamp page size: non-positive -> default, cap at max
        int resolvedSize = size;
        if (resolvedSize <= 0) {
            resolvedSize = defaultPageSize;
        }
        if (resolvedSize > maxPageSize) {
            resolvedSize = maxPageSize;
        }

        // clamp page index to >= 0
        int resolvedPage = Math.max(page, 0);

        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(sortDirection, resolvedSortBy));
        return new PageQuery(pageable, resolvedSortBy, resolvedDirection);
    }
}
