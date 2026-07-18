package com.nikhil.habit_money.habits.controller;

import com.nikhil.habit_money.habits.dto.response.HabitTemplateResponse;
import com.nikhil.habit_money.habits.service.HabitTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/habit-templates")
@RequiredArgsConstructor
public class HabitTemplateController {

    private final HabitTemplateService templateService;

    @GetMapping
    public ResponseEntity<List<HabitTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitTemplateResponse> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }
}
