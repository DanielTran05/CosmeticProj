package com.dtp.cosmemgt.catalog.dto.response;

import com.dtp.cosmemgt.catalog.entity.Category;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    int id;
    int parentId;
    String name;
    String img;
}