package com.nikhil.habit_money.habits.dto.response;

import com.nikhil.habit_money.common.category.dto.response.CategoryResponse;
import com.nikhil.habit_money.habits.enums.HabitFrequency;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class HabitTemplateResponse {

    private UUID id;
    private String title;
    private String description;
    private CategoryResponse category;
    private HabitFrequency frequency;
    private String icon;
}
