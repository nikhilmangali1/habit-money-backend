package com.nikhil.habit_money.habits.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HabitSummaryResponse {

    private String period;
    private int totalCompleted;
    private int totalMissed;
    private int totalSkipped;
    private double completionRate;
    private int currentStreak;
    private int bestStreak;
}
