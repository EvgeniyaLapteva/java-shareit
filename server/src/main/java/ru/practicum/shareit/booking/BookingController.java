package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                             @PathVariable("bookingId") long bookingId,
                             @RequestParam boolean approved) {
        return bookingService.updateBookingStatusByOwner(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto findByBookingId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId") Long bookingId) {
        return bookingService.findByBookingId(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> findAllUsersBookingByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(defaultValue = "0") int from,
                                                          @RequestParam(defaultValue = "10") int size) {
        BookingState bookingState = BookingState.from(state);
        return bookingService.findAllUsersBookingByState(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllBookingsForItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam String state,
                                                       @RequestParam int from,
                                                       @RequestParam int size) {
        BookingState bookingState = BookingState.from(state);
        return bookingService.findAllBookingsForItemsOfUser(userId, bookingState, from, size);
    }
}