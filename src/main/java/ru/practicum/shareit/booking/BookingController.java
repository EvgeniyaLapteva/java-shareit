package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @Valid @RequestBody BookingDto bookingDto) {
        log.info("Запрос на создание бронирования");
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @PathVariable("bookingId") Long bookingId,
                             @RequestParam boolean approved) {
        log.info("Запрос на обновление статуса бронирования");
        return bookingService.updateBookingStatusByOwner(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto findByBookingId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId") Long bookingId) {
        log.info("Запрос на получение бронирования по id");
        return bookingService.findByBookingId(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> findAllUsersBookingByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(defaultValue = "0", required = false)
                                                          @Min(0) int from,
                                                          @RequestParam(defaultValue = "10", required = false)
                                                          @Positive int size) {
        log.info("Запрос на получение списка всех бронирований пользователя");
        return bookingService.findAllUsersBookingByState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllBookingsForItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0", required = false)
                                                       @Min(0) int from,
                                                       @RequestParam(defaultValue = "10", required = false)
                                                       @Positive int size) {
        log.info("Запрос на получение списка бронирований для всех вещей пользователя");
        return bookingService.findAllBookingsForItemsOfUser(userId, state, from, size);
    }
}
