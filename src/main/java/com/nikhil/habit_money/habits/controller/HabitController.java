package com.nikhil.habit_money.habits.controller;

import com.nikhil.habit_money.habits.dto.request.CreateHabitRequest;
import com.nikhil.habit_money.habits.dto.request.HabitLogRequest;
import com.nikhil.habit_money.habits.dto.request.UpdateHabitRequest;
import com.nikhil.habit_money.habits.dto.response.HabitLogResponse;
import com.nikhil.habit_money.habits.dto.response.HabitResponse;
import com.nikhil.habit_money.habits.dto.response.HabitSummaryResponse;
import com.nikhil.habit_money.habits.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    public ResponseEntity<List<HabitResponse>> getUserHabits() {
        return ResponseEntity.ok(habitService.getUserHabits());
    }

    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody CreateHabitRequest request) {
        HabitResponse response = habitService.createHabit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitResponse> getHabit(@PathVariable UUID id) {
        return ResponseEntity.ok(habitService.getHabit(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> updateHabit(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHabitRequest request) {
        return ResponseEntity.ok(habitService.updateHabit(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable UUID id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<HabitLogResponse> logHabit(
            @PathVariable UUID id,
            @Valid @RequestBody HabitLogRequest request) {
        HabitLogResponse response = habitService.logHabit(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/log/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable UUID id, @PathVariable UUID logId) {
        habitService.deleteLog(id, logId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<HabitLogResponse>> getHabitLogs(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(habitService.getHabitLogs(id, startDate, endDate));
    }

    @GetMapping("/summary/weekly")
    public ResponseEntity<HabitSummaryResponse> getWeeklySummary() {
        return ResponseEntity.ok(habitService.getWeeklySummary());
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<HabitSummaryResponse> getMonthlySummary() {
        return ResponseEntity.ok(habitService.getMonthlySummary());
    }

    @GetMapping("/summary/10day")
    public ResponseEntity<HabitSummaryResponse> get10DaySummary() {
        return ResponseEntity.ok(habitService.get10DaySummary());
    }
}
