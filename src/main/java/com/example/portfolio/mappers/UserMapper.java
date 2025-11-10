package com.example.portfolio.mappers;

import com.example.portfolio.dto.UserResponseDto;
import com.example.portfolio.models.User;

public class UserMapper {

    public static UserResponseDto toResponseDto(User u) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setUpdatedAt(u.getUpdatedAt());
        return dto;
    }
}