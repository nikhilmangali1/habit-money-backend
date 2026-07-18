package com.nikhil.habit_money.common.category.repository;

import com.nikhil.habit_money.common.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findByUserIdOrIsPredefinedTrue(UUID userId);
}
