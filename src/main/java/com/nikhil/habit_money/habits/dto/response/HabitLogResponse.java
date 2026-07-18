package com.nikhil.habit_money.habits.dto.response;

import com.nikhil.habit_money.habits.enums.LogStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class HabitLogResponse {

    private UUID id;
    private UUID habitId;
    private LocalDate logDate;
    private LogStatus status;
    private String note;
}
