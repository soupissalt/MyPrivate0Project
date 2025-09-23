package com.record.myprivateproject.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page, int size,
        long totalElements, int totalPages, boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, org.springframework.data.domain.Page<?> p) {
        return new PageResponse<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }
}