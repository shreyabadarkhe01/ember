package com.ember.backend.checkin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

//@Component
@Mapper(componentModel = "spring")
public interface CheckInMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "scaledHabits", ignore = true)
    CheckInDto toDto(CheckIn checkIn);
}