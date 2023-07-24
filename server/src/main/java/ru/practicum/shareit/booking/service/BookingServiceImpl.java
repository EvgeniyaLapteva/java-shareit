package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ru.practicum.shareit.exception.model.ValidateStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (!item.isAvailable()) {
            log.error("Вещь с id = {} уже забронирована", itemId);
            throw new BookingApproveException("Вещь с id = " + itemId + "уже забронирована");
        }
        if (userId.equals(item.getOwner().getId())) {
            log.error("Невозможно забронировать вещь, принадлежащую вам");
            throw new ObjectNotFoundException("Невозможно забронировать вещь, принадлежащую вам");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        log.info("Создано бронирование вещи id = {}", itemId);
        return BookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto updateBookingStatusByOwner(long userId, long bookingId, boolean approved) {
        validateUser(userId);
        Booking booking = validateBooking(bookingId);
        Long itemId = booking.getItem().getId();
        Item item = validateItem(itemId);
        Long ownerId = item.getOwner().getId();
        if (!ownerId.equals(userId)) {
            log.error("Подтверждать бронирование может только собственник вещи");
            throw new ObjectNotFoundException("Подтверждать бронирование может только собственник вещи");
        }
        if (approved && booking.getStatus() == BookingStatus.APPROVED) {
            log.error("Бронирование вещи с id = {} уже одобрено", itemId);
            throw new BookingApproveException("Бронирование вещи с id = " + itemId + " уже одобрено");
        }
        if (!approved && booking.getStatus() == BookingStatus.REJECTED) {
            log.error("Бронирование вещи с id = {} уже было отклонено", itemId);
            throw new BookingApproveException("Бронирование вещи с id = " + itemId + "уже было отклонено");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        log.info("Обновлен статус бронирования вещи id = {}", itemId);
        return BookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingOutDto findByBookingId(Long userId, Long bookingId) {
        validateUser(userId);
        Booking booking = validateBooking(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (userId.equals(bookerId) || userId.equals(ownerId)) {
            log.info("Нашли бронирование id = {}", bookingId);
            return BookingMapper.toBookingOutDto(booking);
        } else {
            log.error("Просматривать информацию о бронировании может только собственник вещи или арендатор");
            throw new ObjectNotFoundException("Просматривать информацию о бронировании может только собственник вещи" +
                    "или арендатор");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> findAllUsersBookingByState(Long userId, String state, int from, int size) {
        validateUser(userId);
        BookingState stateFromRequest = transformStringToState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> usersBooking = new ArrayList<>();

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        PageRequest page = PageRequest.of(from / size, size, sort);
        switch (stateFromRequest) {
            case ALL:
                usersBooking = bookingRepository.findByBookerIdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                usersBooking = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        now, now, page);
                break;
            case PAST:
                usersBooking = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, now, page);
                break;
            case FUTURE:
                usersBooking = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, now, page);
                break;
            case WAITING:
                usersBooking = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,
                        page);
                break;
            case REJECTED:
                usersBooking = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.REJECTED, page);
                break;
            case UNSUPPORTED_STATUS:
                log.error("Получен запрос с неизвестным статусом — {}", state);
                throw new ValidateStateException("Unknown state: UNSUPPORTED_STATUS");
        }
        log.info("Получили список всех бронирований пользователя");
        return usersBooking.stream()
                .map(BookingMapper::toBookingOutDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> findAllBookingsForItemsOfUser(Long userId, String state, int from, int size) {
        validateUser(userId);
        BookingState stateFromRequest = transformStringToState(state);
        LocalDateTime now = LocalDateTime.now();
        if (itemRepository.findByOwnerId(userId).size() == 0) {
            log.error("У пользователя нет вещей для бронирования");
            throw new  ObjectNotFoundException("У пользователя нет вещей для бронирования");
        }
        List<Booking> bookings = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        PageRequest page = PageRequest.of(from / size, size, sort);
        switch (stateFromRequest) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        now, now, page);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, now, page);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,now, page);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.REJECTED, page);
                break;
            case UNSUPPORTED_STATUS:
                log.error("Получен запрос с неизвестным статусом — {}", state);
                throw new ValidateStateException("Unknown state: UNSUPPORTED_STATUS");
        }
        log.info("Получили список бронирований для всех вещей пользователя");
        return bookings.stream()
                .map(BookingMapper::toBookingOutDto).collect(Collectors.toList());
    }

    private BookingState transformStringToState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException exception) {
            log.error("Получен запрос с неизвестным статусом — {}", state);
            throw new ValidateStateException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        " с id = " + userId + " не найден"));
    }

    private Item validateItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException("Вещь с id = " +
                itemId + " не найдена"));
    }

    private Booking validateBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new ObjectNotFoundException("Бронирование с указанным id не найдено"));
    }
}
