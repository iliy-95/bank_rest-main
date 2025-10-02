package com.example.bankcards.repository;

import com.example.bankcards.entity.BankUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<BankUser, UUID> {

  Optional<BankUser> findByUsername(String userName);

  boolean existsByUsername(String userName);
}