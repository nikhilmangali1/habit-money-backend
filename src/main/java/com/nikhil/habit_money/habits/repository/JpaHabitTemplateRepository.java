package com.nikhil.habit_money.habits.repository;

import com.nikhil.habit_money.habits.entity.HabitTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaHabitTemplateRepository extends JpaRepository<HabitTemplateEntity, UUID> {

    List<HabitTemplateEntity> findByIsActiveTrue();
}
