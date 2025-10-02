package com.example.bankcards.repository;

import com.example.bankcards.entity.CardStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardStatusRepository extends JpaRepository<CardStatus, UUID> {

  CardStatus getCardStatusByName(String name);
}