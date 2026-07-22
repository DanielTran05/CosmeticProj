package com.dtp.cosmemgt.core.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    List<T> content;
    int currentPage;         // trang hien tai
    int pageSize;            // tong do phan tu moi trang
    long totalElements;      // tong so bang ghi db
    int totalPages;          // tong so trang
    boolean hasNext;
    boolean hasPrevious;

    public static <T> PageResponse<T> of(Page<T> springPage) {
        return PageResponse.<T>builder()
                .content(springPage.getContent())
                .currentPage(springPage.getNumber())
                .pageSize(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .hasNext(springPage.hasNext())
                .hasPrevious(springPage.hasPrevious())
                .build();
    }
}