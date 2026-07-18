package com.nikhil.habit_money.habits.dto.request;

import com.nikhil.habit_money.habits.enums.LogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HabitLogRequest {

    @NotNull(message = "Log date is required")
    private LocalDate logDate;

    @NotNull(message = "Status is required")
    private LogStatus status;

    private String note;
}
