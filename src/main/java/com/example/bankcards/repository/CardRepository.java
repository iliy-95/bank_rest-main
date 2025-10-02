package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {


  @Query("""
        SELECT c FROM Card c 
        JOIN FETCH c.bankUser bu
        JOIN FETCH c.status s
        WHERE bu.username = :username 
        AND (:search IS NULL OR c.lastFourDigits LIKE %:search%)
    """)
  Page<Card> findByUsernameAndSearch(@Param("username") String username,
      @Param("search") String search,
      Pageable pageable);

  @Query("""
        SELECT c FROM Card c 
        JOIN FETCH c.bankUser bu
        JOIN FETCH c.status s
        WHERE (:search IS NULL OR c.lastFourDigits LIKE %:search%)
    """)
  Page<Card> findAllWithSearch(@Param("search") String search, Pageable pageable);

  @Query("""
        SELECT c FROM Card c 
        JOIN FETCH c.bankUser bu
        JOIN FETCH c.status s
        WHERE c.id = :id
    """)
  Optional<Card> findByIdWithUserAndStatus(@Param("id") UUID id);
}

