package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.model.ValidateBookingsDatesException;
import ru.practicum.shareit.exception.model.ValidateStateException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new ValidateStateException("Unknown state: UNSUPPORTED_STATUS"));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestBody @Valid BookItemRequestDto requestDto) {
		if (!requestDto.getEnd().isAfter(requestDto.getStart()) || requestDto.getEnd().equals(requestDto.getStart())) {
			log.error("Проверьте даты начала и окончания бронирования");
			throw new ValidateBookingsDatesException("Проверьте даты начала и окончания бронирования");
		}
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.bookItem(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBookingStatus(@RequestHeader("X-Sharer-User-Id") long userId,
													  @PathVariable("bookingId") long bookingId,
													  @RequestParam boolean approved) {
		log.info("Запрос на обновление статуса бронирования");
		return bookingClient.updateBookingStateByOwner(userId, bookingId, approved);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> findAllBookingsForItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
																@RequestParam(name = "state", defaultValue = "ALL")
																String stateParam, @PositiveOrZero @RequestParam(
																name = "from", defaultValue = "0") int from, @Positive
																@RequestParam(name = "size", defaultValue = "10")
																int size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new ValidateStateException("Unknown state: UNSUPPORTED_STATUS"));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.findAllBookingsForItemsOfUser(userId, state, from, size);
	}
}
