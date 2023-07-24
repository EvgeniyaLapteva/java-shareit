package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exception.model.ValidationException;

public enum BookingState {

    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    UNSUPPORTED_STATUS;

    public static BookingState from(String stateParam) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stateParam)) {
                return state;
            }
        }
        throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
    }
}
