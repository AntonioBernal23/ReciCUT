package com.user_service.events;

public record UserRegisteredEvent(
        Long userId,
        String name,
        String lastName,
        String picture
) {}