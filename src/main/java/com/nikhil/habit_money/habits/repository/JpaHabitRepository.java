package com.nikhil.habit_money.habits.repository;

import com.nikhil.habit_money.habits.entity.HabitEntity;
import com.nikhil.habit_money.habits.enums.HabitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaHabitRepository extends JpaRepository<HabitEntity, UUID> {

    List<HabitEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<HabitEntity> findByUserIdAndStatus(UUID userId, HabitStatus status);
}
