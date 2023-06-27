package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

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
        return null;
    }

    @Override
    public BookingOutDto updateBookingStatusByOwner(Long userId, Long bookingId, boolean approved) {
        return null;
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
}
