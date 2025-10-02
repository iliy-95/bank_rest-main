package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum CardStatusEnum {
    ACTIVE("11111111-1111-1111-1111-111111111111"),
    BLOCKED("22222222-2222-2222-2222-222222222222"),
    EXPIRED("33333333-3333-3333-3333-333333333333");
    
    private final String  id;
    
    CardStatusEnum(String  id) {
        this.id = id;
    }
    
    public String  getId() {
        return id;
    }
}