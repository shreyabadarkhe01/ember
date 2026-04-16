package com.ember.backend.habit;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface HabitMapper {

    @Mapping(source = "user.id", target = "userId")
    HabitDto toDto(Habit habit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Habit toEntity(HabitDto dto);
}