package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.BookingApproveException;
import ru.practicum.shareit.exception.model.ValidateBookingsDatesException;
import ru.practicum.shareit.exception.model.ValidateStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;


import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private static final int FROM = 0;

    private static final int SIZE = 1;

    @Mock
    private BookingRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl service;

    private User owner;

    private User booker;

    private Item item;

    private Booking booking;

    private BookingDto bookingDto;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@mail.ru");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@mail.ru");

        item = new Item();
        item.setId(1L);
        item.setName("knife");
        item.setDescription("big");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(NOW.plusHours(1));
        booking.setEnd(NOW.plusDays(2));

        bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    @Test
    void shouldCreateBooking() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(repository.save(any()))
                .thenReturn(booking);
        BookingOutDto bookingOutDto = service.create(booker.getId(), bookingDto);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        verify(repository, times(1))
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateBookingIfUserNotExist() {
        Long userId = 0L;
        String errorMessage = "Пользователь" +
                " с id = " + userId + " не найден";
        when(userRepository.findById(anyLong()))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.create(userId, bookingDto));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateBookingIfItemNotExist() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        long itemId = 0L;
        String errorMessage = "Вещь с id = " +
                itemId + " не найдена";
        when(itemRepository.findById(anyLong()))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.create(booker.getId(), bookingDto));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateBookingIfWrongDates() {
        String errorMessage = "Проверьте даты начала и окончания бронирования";
        bookingDto.setStart(booking.getEnd());
        bookingDto.setEnd(booking.getStart());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        ValidateBookingsDatesException exception = assertThrows(ValidateBookingsDatesException.class,
                () -> service.create(booker.getId(), bookingDto));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateBookingIfItemIsNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        String errorMessage = "Вещь с id = " + item.getId() + "уже забронирована";

        BookingApproveException exception = assertThrows(BookingApproveException.class,
                () -> service.create(booker.getId(), bookingDto));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateBookingIfBookerIsOwner() {
        item.setOwner(booker);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        String errorMessage = "Невозможно забронировать вещь, принадлежащую вам";

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.create(booker.getId(), bookingDto));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldUpdateBookingStatusByOwnerWhenApproved() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(repository.save(any()))
                .thenReturn(booking);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        BookingOutDto bookingOutDto = service.updateBookingStatusByOwner(owner.getId(), booking.getId(), true);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        assertEquals(BookingStatus.APPROVED, bookingOutDto.getStatus());
        verify(repository, times(1))
                .save(any());
    }

    @Test
    void shouldUpdateBookingStatusByOwnerWhenRejected() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(repository.save(any()))
                .thenReturn(booking);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        BookingOutDto bookingOutDto = service.updateBookingStatusByOwner(owner.getId(), booking.getId(), false);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        assertEquals(BookingStatus.REJECTED, bookingOutDto.getStatus());
        verify(repository, times(1))
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateBookingStatusIfBookingDoesNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.updateBookingStatusByOwner(owner.getId(), booking.getId(), true));

        assertEquals("Бронирование с указанным id не найдено", exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateBookingStatusIfUserIsNotOwner() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        item.setOwner(booker);
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.updateBookingStatusByOwner(owner.getId(), booking.getId(), true));

        assertEquals("Подтверждать бронирование может только собственник вещи", exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateBookingStatusIfAlreadyConfirmed() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        booking.setStatus(BookingStatus.APPROVED);

        BookingApproveException exception = assertThrows(BookingApproveException.class,
                () -> service.updateBookingStatusByOwner(owner.getId(), booking.getId(), true));

        assertEquals("Бронирование вещи с id = " + item.getId() + " уже одобрено", exception.getMessage());

        booking.setStatus(BookingStatus.REJECTED);

        BookingApproveException exception1 = assertThrows(BookingApproveException.class,
                () -> service.updateBookingStatusByOwner(owner.getId(), booking.getId(), false));

        assertEquals("Бронирование вещи с id = " + item.getId() + "уже было отклонено",
                exception1.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldFindByBookingId() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        BookingOutDto bookingOutDto = service.findByBookingId(owner.getId(), booking.getId());

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void shouldThrowExceptionWhenGetBookingByIdIfUserIsNotOwner() {
        User newUser = new User();
        newUser.setId(3L);
        newUser.setName("third");
        newUser.setEmail("emeil@email.ru");
        Long userId = newUser.getId();
        Long bookingId = booking.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(newUser));
        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.findByBookingId(userId, bookingId));

        assertEquals("Просматривать информацию о бронировании может только собственник вещи" +
                "или арендатор", exception.getMessage());
    }

    @Test
    void shouldFindAllUsersBookingByStateAll() {
        Long userId = booker.getId();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(repository.findByBookerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllUsersBookingByState(userId, BookingState.ALL, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void shouldFindAllUsersBookingByStateCurrent() {
        Long userId = booker.getId();
        booking.setEnd(NOW.plusHours(10));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(repository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllUsersBookingByState(userId, BookingState.CURRENT, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    void shouldFindAllUsersBookingByStatePast() {
        Long userId = booker.getId();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(repository.findByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllUsersBookingByState(userId, BookingState.PAST, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldFindAllUsersBookingByStateFuture() {
        Long userId = booker.getId();
        booking.setStatus(BookingStatus.WAITING);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(repository.findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllUsersBookingByState(userId, BookingState.FUTURE, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        assertEquals(BookingStatus.WAITING, bookingOutDtos.get(0).getStatus());
        verify(repository, times(1))
                .findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldFindAllUsersBookingByStateWaiting() {
        Long userId = booker.getId();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(repository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllUsersBookingByState(userId, BookingState.WAITING, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldFindAllUsersBookingByStateRejected() {
        Long userId = booker.getId();
        booking.setStatus(BookingStatus.REJECTED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(repository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllUsersBookingByState(userId, BookingState.REJECTED, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        assertEquals(BookingStatus.REJECTED, bookingOutDtos.get(0).getStatus());
        verify(repository, times(1))
                .findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenFindAllUsersBookingByStateWithUnsupportedState() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        String errorMessage = "Unknown state: UNSUPPORTED_STATUS";

        ValidateStateException exception = assertThrows(ValidateStateException.class,
                () -> service.findAllUsersBookingByState(userId, BookingState.UNSUPPORTED_STATUS, FROM,
                        SIZE));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFindAllUsersBookingByStateWithUnknownState() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        String errorMessage = "Unknown state: UNSUPPORTED_STATUS";

        ValidateStateException exception = assertThrows(ValidateStateException.class,
                () -> service.findAllUsersBookingByState(userId, BookingState.UNSUPPORTED_STATUS, FROM,
                        SIZE));

        assertEquals(errorMessage, exception.getMessage());
    }


    @Test
    void shouldFindAllBookingsForItemsOfUserWithStateAll() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        when(repository.findByItemOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllBookingsForItemsOfUser(userId, BookingState.ALL, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByItemOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserWithStateCurrent() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        when(repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllBookingsForItemsOfUser(userId, BookingState.CURRENT, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserWithStatePast() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        when(repository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllBookingsForItemsOfUser(userId, BookingState.PAST, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserWithStateFuture() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        when(repository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllBookingsForItemsOfUser(userId, BookingState.FUTURE, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByItemOwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserWithStateWaiting() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        booking.setStatus(BookingStatus.WAITING);
        when(repository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllBookingsForItemsOfUser(userId, BookingState.WAITING, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserWithStateRejected() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        booking.setStatus(BookingStatus.REJECTED);
        when(repository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findAllBookingsForItemsOfUser(userId, BookingState.REJECTED, FROM, SIZE);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());
        verify(repository, times(1))
                .findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenFindAllBookingsForItemsOfUserWithUnsupportedState() {
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(item));
        String errorMessage = "Unknown state: UNSUPPORTED_STATUS";

        ValidateStateException exception = assertThrows(ValidateStateException.class,
                () -> service.findAllBookingsForItemsOfUser(userId, BookingState.UNSUPPORTED_STATUS, FROM,
                        SIZE));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFindAllBookingsForItemsOfUserIfUserHasNotItemsForBooking() {
        String errorMessage = "У пользователя нет вещей для бронирования";
        Long userId = booker.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findByOwnerId(anyLong()))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.findAllBookingsForItemsOfUser(userId, BookingState.ALL, FROM, SIZE));

        assertEquals(errorMessage, exception.getMessage());
    }
}