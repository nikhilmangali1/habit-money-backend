package com.nikhil.habit_money.habits.service;

import com.nikhil.habit_money.common.category.dto.response.CategoryResponse;
import com.nikhil.habit_money.common.category.entity.CategoryEntity;
import com.nikhil.habit_money.common.category.repository.JpaCategoryRepository;
import com.nikhil.habit_money.common.exceptions.ResourceNotFoundException;
import com.nikhil.habit_money.common.exceptions.ValidationException;
import com.nikhil.habit_money.habits.dto.request.CreateHabitRequest;
import com.nikhil.habit_money.habits.dto.request.HabitLogRequest;
import com.nikhil.habit_money.habits.dto.request.UpdateHabitRequest;
import com.nikhil.habit_money.habits.dto.response.HabitLogResponse;
import com.nikhil.habit_money.habits.dto.response.HabitResponse;
import com.nikhil.habit_money.habits.dto.response.HabitSummaryResponse;
import com.nikhil.habit_money.habits.entity.HabitEntity;
import com.nikhil.habit_money.habits.entity.HabitLogEntity;
import com.nikhil.habit_money.habits.entity.HabitTemplateEntity;
import com.nikhil.habit_money.habits.enums.HabitFrequency;
import com.nikhil.habit_money.habits.enums.HabitStatus;
import com.nikhil.habit_money.habits.enums.LogStatus;
import com.nikhil.habit_money.habits.repository.JpaHabitLogRepository;
import com.nikhil.habit_money.habits.repository.JpaHabitRepository;
import com.nikhil.habit_money.habits.repository.JpaHabitTemplateRepository;
import com.nikhil.habit_money.security.CurrentUser;
import com.nikhil.habit_money.users.entity.UserEntity;
import com.nikhil.habit_money.users.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitService {

    private final JpaHabitRepository habitRepository;
    private final JpaHabitLogRepository habitLogRepository;
    private final JpaHabitTemplateRepository templateRepository;
    private final JpaCategoryRepository categoryRepository;
    private final JpaUserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<HabitResponse> getUserHabits() {
        UUID userId = currentUser.getUserId();
        List<HabitEntity> habits = habitRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return habits.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse getHabit(UUID id) {
        HabitEntity entity = findOwnedHabit(id);
        return toResponse(entity);
    }

    @Transactional
    public HabitResponse createHabit(CreateHabitRequest request) {
        UUID userId = currentUser.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        HabitEntity.HabitEntityBuilder builder = HabitEntity.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .frequency(request.getFrequency() != null ? request.getFrequency() : HabitFrequency.DAILY)
                .status(HabitStatus.ACTIVE)
                .startDate(LocalDate.now())
                .streakCount(0)
                .bestStreak(0)
                .totalCompletions(0)
                .totalMissed(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());

        if (request.getTemplateId() != null) {
            HabitTemplateEntity template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
            builder.template(template);
            if (request.getTitle() == null) {
                builder.title(template.getTitle());
            }
            if (request.getDescription() == null) {
                builder.description(template.getDescription());
            }
            if (request.getCategoryId() == null && template.getCategory() != null) {
                builder.category(template.getCategory());
            }
            if (request.getFrequency() == null) {
                builder.frequency(template.getFrequency());
            }
        }

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            builder.category(category);
        }

        if (request.getTitle() == null && request.getTemplateId() == null) {
            throw new ValidationException("Title is required when no template is provided");
        }

        HabitEntity saved = habitRepository.save(builder.build());
        return toResponse(saved);
    }

    @Transactional
    public HabitResponse updateHabit(UUID id, UpdateHabitRequest request) {
        HabitEntity entity = findOwnedHabit(id);

        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getFrequency() != null) entity.setFrequency(request.getFrequency());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            entity.setCategory(category);
        }

        entity.setUpdatedAt(LocalDateTime.now());
        habitRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void deleteHabit(UUID id) {
        HabitEntity entity = findOwnedHabit(id);
        habitRepository.delete(entity);
    }

    @Transactional
    public HabitLogResponse logHabit(UUID habitId, HabitLogRequest request) {
        HabitEntity habit = findOwnedHabit(habitId);

        if (habitLogRepository.existsByHabitIdAndLogDate(habitId, request.getLogDate())) {
            throw new ValidationException("Log already exists for this date");
        }

        HabitLogEntity log = HabitLogEntity.builder()
                .habit(habit)
                .logDate(request.getLogDate())
                .status(request.getStatus())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        habitLogRepository.save(log);

        if (request.getStatus() == LogStatus.COMPLETED) {
            habit.setTotalCompletions(habit.getTotalCompletions() + 1);
        } else if (request.getStatus() == LogStatus.MISSED) {
            habit.setTotalMissed(habit.getTotalMissed() + 1);
        }

        int streak = recalculateStreak(habitId);
        habit.setStreakCount(streak);
        if (streak > habit.getBestStreak()) {
            habit.setBestStreak(streak);
        }

        habit.setUpdatedAt(LocalDateTime.now());
        habitRepository.save(habit);

        return toLogResponse(log);
    }

    @Transactional
    public void deleteLog(UUID habitId, UUID logId) {
        HabitEntity habit = findOwnedHabit(habitId);
        HabitLogEntity log = habitLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));

        if (!log.getHabit().getId().equals(habitId)) {
            throw new ValidationException("Log does not belong to this habit");
        }

        if (log.getStatus() == LogStatus.COMPLETED) {
            habit.setTotalCompletions(Math.max(0, habit.getTotalCompletions() - 1));
        } else if (log.getStatus() == LogStatus.MISSED) {
            habit.setTotalMissed(Math.max(0, habit.getTotalMissed() - 1));
        }

        habitLogRepository.delete(log);

        int streak = recalculateStreak(habitId);
        habit.setStreakCount(streak);

        habit.setUpdatedAt(LocalDateTime.now());
        habitRepository.save(habit);
    }

    @Transactional(readOnly = true)
    public List<HabitLogResponse> getHabitLogs(UUID habitId, LocalDate startDate, LocalDate endDate) {
        HabitEntity habit = findOwnedHabit(habitId);
        List<HabitLogEntity> logs;

        if (startDate != null && endDate != null) {
            logs = habitLogRepository.findByHabitIdAndLogDateBetweenOrderByLogDateAsc(habitId, startDate, endDate);
        } else {
            logs = habitLogRepository.findByHabitIdOrderByLogDateDesc(habitId);
        }

        return logs.stream()
                .map(this::toLogResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitSummaryResponse getDailySummary() {
        LocalDate today = LocalDate.now();
        return computeSummary(today, today, "Daily");
    }

    @Transactional(readOnly = true)
    public HabitSummaryResponse getWeeklySummary() {
        UUID userId = currentUser.getUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(7, ChronoUnit.DAYS);
        return computeSummary(startDate, endDate, "Weekly");
    }

    @Transactional(readOnly = true)
    public HabitSummaryResponse getMonthlySummary() {
        UUID userId = currentUser.getUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(30, ChronoUnit.DAYS);
        return computeSummary(startDate, endDate, "Monthly");
    }

    @Transactional(readOnly = true)
    public HabitSummaryResponse get10DaySummary() {
        UUID userId = currentUser.getUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(10, ChronoUnit.DAYS);
        return computeSummary(startDate, endDate, "10-Day");
    }

    private HabitSummaryResponse computeSummary(LocalDate startDate, LocalDate endDate, String period) {
        UUID userId = currentUser.getUserId();
        List<HabitEntity> habits = habitRepository.findByUserIdAndStatus(userId, HabitStatus.ACTIVE);
        List<UUID> habitIds = habits.stream().map(HabitEntity::getId).toList();

        List<HabitLogEntity> logs = habitLogRepository
                .findByHabitIdInAndLogDateBetweenOrderByLogDateAsc(habitIds, startDate, endDate);

        long completed = logs.stream().filter(l -> l.getStatus() == LogStatus.COMPLETED).count();
        long missed = logs.stream().filter(l -> l.getStatus() == LogStatus.MISSED).count();
        long skipped = logs.stream().filter(l -> l.getStatus() == LogStatus.SKIPPED).count();

        int daysInPeriod = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long expected = 0;
        for (HabitEntity habit : habits) {
            switch (habit.getFrequency()) {
                case DAILY:   expected += daysInPeriod; break;
                case WEEKLY:  expected += (int) Math.ceil((double) daysInPeriod / 7); break;
                case MONTHLY: expected += (int) Math.ceil((double) daysInPeriod / 30); break;
            }
        }
        double rate = expected > 0 ? (double) completed / expected * 100 : 0;

        int bestStreak = habits.stream().mapToInt(HabitEntity::getBestStreak).max().orElse(0);
        int currentStreak = habits.stream().mapToInt(HabitEntity::getStreakCount).max().orElse(0);

        return HabitSummaryResponse.builder()
                .period(period)
                .totalCompleted((int) completed)
                .totalMissed((int) missed)
                .totalSkipped((int) skipped)
                .completionRate(Math.round(rate * 10.0) / 10.0)
                .currentStreak(currentStreak)
                .bestStreak(bestStreak)
                .build();
    }

    private int recalculateStreak(UUID habitId) {
        List<HabitLogEntity> logs = habitLogRepository.findByHabitIdOrderByLogDateDesc(habitId);
        int streak = 0;
        for (HabitLogEntity log : logs) {
            if (log.getStatus() == LogStatus.COMPLETED) {
                streak++;
            } else if (log.getStatus() == LogStatus.MISSED) {
                break;
            }
        }
        return streak;
    }

    private HabitEntity findOwnedHabit(UUID habitId) {
        HabitEntity entity = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
        UUID userId = currentUser.getUserId();
        if (!entity.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Habit not found");
        }
        return entity;
    }

    private HabitResponse toResponse(HabitEntity entity) {
        UUID userId = currentUser.getUserId();
        boolean completedToday = habitLogRepository
                .existsByHabitIdAndLogDate(entity.getId(), LocalDate.now());

        return HabitResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory() != null
                        ? CategoryResponse.builder()
                                .id(entity.getCategory().getId())
                                .name(entity.getCategory().getName())
                                .type(entity.getCategory().getType())
                                .icon(entity.getCategory().getIcon())
                                .predefined(entity.getCategory().isPredefined())
                                .build()
                        : null)
                .frequency(entity.getFrequency())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .streakCount(entity.getStreakCount())
                .bestStreak(entity.getBestStreak())
                .totalCompletions(entity.getTotalCompletions())
                .totalMissed(entity.getTotalMissed())
                .completedToday(completedToday)
                .build();
    }

    private HabitLogResponse toLogResponse(HabitLogEntity entity) {
        return HabitLogResponse.builder()
                .id(entity.getId())
                .habitId(entity.getHabit().getId())
                .logDate(entity.getLogDate())
                .status(entity.getStatus())
                .note(entity.getNote())
                .build();
    }
}
