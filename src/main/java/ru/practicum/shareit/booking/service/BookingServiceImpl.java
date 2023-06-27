package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.BookingApproveException;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidateBookingsDatesException;
import ru.practicum.shareit.exception.model.ValidateStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingOutDto create(Long userId, BookingDto bookingDto) {
        User user = validateUser(userId);
        Long itemId = bookingDto.getItemId();
        Item item = validateItem(itemId);
        validateBookingDates(bookingDto);
        if (!item.isAvailable()) {
            throw new BookingApproveException("Вещь с id = " + itemId + "уже забронирована");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new ObjectNotFoundException("Невозможно забронировать вещь, принадлежащую вам");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        return BookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto updateBookingStatusByOwner(Long userId, Long bookingId, boolean approved) {
        validateUser(userId);
        Booking booking = validateBooking(bookingId);
        Long itemId = booking.getItem().getId();
        Item item = validateItem(itemId);
        Long ownerId = item.getOwner().getId();
        if (ownerId.equals(userId)) {
            if (approved && booking.getStatus() == BookingStatus.APPROVED) {
                throw new BookingApproveException("Вещь с id = " + itemId + " уже забронирована");
            }
            if (!approved && booking.getStatus() == BookingStatus.REJECTED) {
                throw new BookingApproveException("Бронирование вещи с id = " + itemId + "уже было отклонено");
            }
            if (approved) {
                booking.setStatus(BookingStatus.APPROVED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
        }
        return BookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto findByBookingId(Long userId, Long bookingId) {
        return null;
    }

    @Override
    public List<BookingOutDto> findAllUsersBookingByState(Long userId, String state) {
        return null;
    }

    @Override
    public List<BookingOutDto> findAllBookingsForItemsOfUser(Long userId, String state) {
        return null;
    }

    private void validateBookingDates(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (!end.isAfter(start) || end.equals(start)) {
            throw new ValidateBookingsDatesException("Проверьте даты начала и окончания бронирования");
        }
    }

    private void validateState(String state) {
        for (BookingState bookingState : BookingState.values()) {
            if (state.equals(bookingState.name())) {
                throw new ValidateStateException("Статус "+ state + "не существует");
            }
        }
    }

    private User validateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        " с id = " + userId + " не найден"));
        return user;
    }

    private Item validateItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException("Вещь с id = " +
                itemId + " не найдена"));
        return item;
    }

    private Booking validateBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).
                orElseThrow(() -> new ObjectNotFoundException("Бронирование с id = " + bookingId + "не найдено"));
        return booking;
    }
}
