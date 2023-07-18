package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class BookingDto {

    private Long id;

    @NotNull(message = "Не указана дата начала бронирования")
    @FutureOrPresent(message = "Дата начала бронирования должна быть в настоящем или будещем времени")
    private LocalDateTime start;

    @NotNull(message = "Не указана дата окончания бронирования")
    @FutureOrPresent(message = "Дата окончания бронирования должна быть в настоящем или будещем времени")
    private LocalDateTime end;

    @NotNull(message = "Не указан идентификатор вещи")
    private Long itemId;

    private Long bookerId;

    private BookingStatus status;
}
