package com.clinic.clinic.Entity.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private String sortBy;
    private String direction;

    /**
     * Builds a JSON-friendly page wrapper from a Spring {@link Page} and the
     * already-mapped DTO content, echoing back the resolved sort information.
     */
    public static <E, D> PageResponse<D> from(Page<E> page, List<D> mappedContent, String sortBy, String direction) {
        return PageResponse.<D>builder()
                .content(mappedContent)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .sortBy(sortBy)
                .direction(direction)
                .build();
    }
}
