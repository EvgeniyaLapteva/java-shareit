package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
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
                                @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @PathVariable("bookingId") Long bookingId,
                             @RequestParam boolean approved) {
        return null;
    }

    @GetMapping("/{bookingId}")
    public BookingDto findByBookingId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId") Long bookingId) {
        return null;
    }

    @GetMapping
    public List<BookingDto> findAllUsersBookingByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(defaultValue = "ALL") String state) {
        return null;
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllBookingsForItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "ALL") String state) {
        return null;
    }

}
