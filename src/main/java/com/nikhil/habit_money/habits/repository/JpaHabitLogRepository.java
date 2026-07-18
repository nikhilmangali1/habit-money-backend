package com.nikhil.habit_money.habits.repository;

import com.nikhil.habit_money.habits.entity.HabitLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JpaHabitLogRepository extends JpaRepository<HabitLogEntity, UUID> {

    List<HabitLogEntity> findByHabitIdOrderByLogDateDesc(UUID habitId);

    List<HabitLogEntity> findByHabitIdAndLogDateBetweenOrderByLogDateAsc(
            UUID habitId, LocalDate startDate, LocalDate endDate);

    boolean existsByHabitIdAndLogDate(UUID habitId, LocalDate logDate);

    List<HabitLogEntity> findByHabitIdInAndLogDateBetweenOrderByLogDateAsc(
            List<UUID> habitIds, LocalDate startDate, LocalDate endDate);
}
