package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "card_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardStatus {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String name;
}
