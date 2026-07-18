package com.nikhil.habit_money.habits.dto.response;

import com.nikhil.habit_money.common.category.dto.response.CategoryResponse;
import com.nikhil.habit_money.habits.enums.HabitFrequency;
import com.nikhil.habit_money.habits.enums.HabitStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class HabitResponse {

    private UUID id;
    private String title;
    private String description;
    private CategoryResponse category;
    private HabitFrequency frequency;
    private HabitStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int streakCount;
    private int bestStreak;
    private int totalCompletions;
    private int totalMissed;
    private boolean completedToday;
}
