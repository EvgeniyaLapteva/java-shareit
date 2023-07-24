package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {

    BookingOutDto create(Long userId, BookingDto bookingDto);

    BookingOutDto updateBookingStatusByOwner(long userId, long bookingId, boolean approved);

    BookingOutDto findByBookingId(Long userId, Long bookingId);

    List<BookingOutDto> findAllUsersBookingByState(Long userId, String state, int from, int size);

    List<BookingOutDto> findAllBookingsForItemsOfUser(Long userId, String state, int from, int size);
}
