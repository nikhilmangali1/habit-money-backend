package com.nikhil.habit_money.auth.repository;

import com.nikhil.habit_money.auth.entity.RefreshTokenEntity;
import com.nikhil.habit_money.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    void deleteAllByUser(UserEntity user);
}
