package com.nikhil.habit_money.habits.dto.request;

import com.nikhil.habit_money.habits.enums.HabitFrequency;
import com.nikhil.habit_money.habits.enums.HabitStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateHabitRequest {

    private String title;
    private String description;
    private UUID categoryId;
    private HabitFrequency frequency;
    private HabitStatus status;
}
