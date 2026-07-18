package com.nikhil.habit_money.common.category.service;

import com.nikhil.habit_money.common.category.dto.request.CreateCategoryRequest;
import com.nikhil.habit_money.common.category.dto.response.CategoryResponse;
import com.nikhil.habit_money.common.category.entity.CategoryEntity;
import com.nikhil.habit_money.common.category.enums.CategoryType;
import com.nikhil.habit_money.common.category.repository.JpaCategoryRepository;
import com.nikhil.habit_money.common.exceptions.BadRequestException;
import com.nikhil.habit_money.common.exceptions.ResourceNotFoundException;
import com.nikhil.habit_money.security.CurrentUser;
import com.nikhil.habit_money.users.entity.UserEntity;
import com.nikhil.habit_money.users.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final JpaCategoryRepository categoryRepository;
    private final JpaUserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(CategoryType type) {
        UUID userId = currentUser.getUserId();

        List<CategoryEntity> categories;
        if (type != null) {
            categories = categoryRepository.findByUserIdOrIsPredefinedTrue(userId).stream()
                    .filter(c -> c.getType() == type)
                    .toList();
        } else {
            categories = categoryRepository.findByUserIdOrIsPredefinedTrue(userId);
        }

        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        UUID userId = currentUser.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CategoryEntity entity = CategoryEntity.builder()
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .isPredefined(false)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        categoryRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (entity.isPredefined()) {
            throw new BadRequestException("Cannot delete predefined categories");
        }

        UUID userId = currentUser.getUserId();
        if (!entity.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cannot delete another user's category");
        }

        categoryRepository.delete(entity);
    }

    private CategoryResponse toResponse(CategoryEntity entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .icon(entity.getIcon())
                .predefined(entity.isPredefined())
                .build();
    }
}
