package com.nikhil.habit_money.habits.dto.request;

import com.nikhil.habit_money.habits.enums.HabitFrequency;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateHabitRequest {

    private UUID templateId;

    private String title;

    private String description;

    private UUID categoryId;

    private HabitFrequency frequency;
}
