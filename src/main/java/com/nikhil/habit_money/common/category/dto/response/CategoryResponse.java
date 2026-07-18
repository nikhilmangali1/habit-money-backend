package com.nikhil.habit_money.common.category.dto.response;

import com.nikhil.habit_money.common.category.enums.CategoryType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryResponse {

    private UUID id;
    private String name;
    private CategoryType type;
    private String icon;
    private boolean predefined;
}
