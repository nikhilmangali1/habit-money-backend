package com.nikhil.habit_money.habits.service;

import com.nikhil.habit_money.common.category.dto.response.CategoryResponse;
import com.nikhil.habit_money.common.exceptions.ResourceNotFoundException;
import com.nikhil.habit_money.habits.dto.response.HabitTemplateResponse;
import com.nikhil.habit_money.habits.entity.HabitTemplateEntity;
import com.nikhil.habit_money.habits.repository.JpaHabitTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HabitTemplateService {

    private final JpaHabitTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public List<HabitTemplateResponse> getAllTemplates() {
        return templateRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitTemplateResponse getTemplate(UUID id) {
        HabitTemplateEntity entity = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return toResponse(entity);
    }

    private HabitTemplateResponse toResponse(HabitTemplateEntity entity) {
        return HabitTemplateResponse.builder()
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
                .icon(entity.getIcon())
                .build();
    }
}
