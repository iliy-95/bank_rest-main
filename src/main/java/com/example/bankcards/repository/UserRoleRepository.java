package com.example.bankcards.repository;

import com.example.bankcards.entity.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

  Optional<UserRole> findByName(String name);
}