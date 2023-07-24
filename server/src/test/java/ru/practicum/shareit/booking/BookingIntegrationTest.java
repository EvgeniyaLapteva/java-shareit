package ru.practicum.shareit.booking;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
public class BookingIntegrationTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private BookingService service;

    private static final LocalDateTime NOW = LocalDateTime.now();

    private User owner;

    private User booker;

    private Item item;

    private Item secondItem;

    private Booking booking;

    private Booking secondBooking;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setName("Tom");
        owner.setEmail("tom@mail.ru");
        manager.persist(owner);

        booker = new User();
        booker.setName("Rick");
        booker.setEmail("rick@mail.ru");
        manager.persist(booker);

        item = new Item();
        item.setName("tool");
        item.setDescription("good");
        item.setAvailable(true);
        item.setOwner(owner);
        manager.persist(item);

        secondItem = new Item();
        secondItem.setName("anotherTool");
        secondItem.setDescription("very small");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner);
        manager.persist(secondItem);

        Booking bookingJustSaved = new Booking();
        bookingJustSaved.setItem(item);
        bookingJustSaved.setBooker(booker);
        bookingJustSaved.setStatus(BookingStatus.APPROVED);
        bookingJustSaved.setStart(NOW.minusDays(5));
        bookingJustSaved.setEnd(NOW.plusDays(5));
        manager.persist(bookingJustSaved);

        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(NOW.plusDays(5));
        booking.setEnd(NOW.plusDays(6));
        manager.persist(booking);

        secondBooking = new Booking();
        secondBooking.setItem(secondItem);
        secondBooking.setBooker(booker);
        secondBooking.setStatus(BookingStatus.REJECTED);
        secondBooking.setStart(NOW.minusDays(2));
        secondBooking.setEnd(NOW.minusDays(1));
        manager.persist(secondBooking);

        Booking anotherBooking = new Booking();
        anotherBooking.setItem(secondItem);
        anotherBooking.setBooker(booker);
        anotherBooking.setStatus(BookingStatus.CANCELED);
        anotherBooking.setStart(NOW.minusDays(1));
        anotherBooking.setEnd(NOW.plusDays(1));
        manager.persist(anotherBooking);
    }

    @Test
    void shouldCreateBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(NOW.plusDays(5))
                .end(NOW.plusDays(10))
                .build();

        BookingOutDto result = service.create(booker.getId(), dto);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        Assertions.assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        Assertions.assertThat(result.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void shouldUpdateBookingWhenApprove() {
        Assertions.assertThat(service.findByBookingId(booker.getId(), booking.getId()))
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

        BookingOutDto result = service.updateBookingStatusByOwner(owner.getId(), booking.getId(), true);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);
        }

    @Test
    void shouldUpdateBookingWhenReject() {
        Assertions.assertThat(service.findByBookingId(booker.getId(), booking.getId()))
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

        BookingOutDto result = service.updateBookingStatusByOwner(owner.getId(), booking.getId(), false);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED);
    }

    @Test
    void shouldGetBookingById() {
        BookingOutDto result = service.findByBookingId(booker.getId(), secondBooking.getId());

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("id", secondBooking.getId());
        Assertions.assertThat(result.getBooker())
                .hasFieldOrPropertyWithValue("id", booker.getId());
    }

    @Test
    void shouldGetAllBookingIfStateIsAll() {
        List<BookingOutDto> result = service.findAllUsersBookingByState(booker.getId(), BookingState.ALL, 0, 10);

        Assertions.assertThat(result).isNotEmpty().hasSize(4);
    }

    @Test
    void shouldGetAllBookingIfStateIsCurrent() {
        List<BookingOutDto> result = service.findAllUsersBookingByState(booker.getId(), BookingState.CURRENT, 0, 10);

        Assertions.assertThat(result).isNotEmpty().hasSize(2);
    }

    @Test
    void shouldGetAllBookingIfStateIsPast() {
        List<BookingOutDto> result = service.findAllUsersBookingByState(booker.getId(), BookingState.PAST, 0, 10);

        Assertions.assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldGetAllBookingIfStateIsFuture() {
        List<BookingOutDto> result = service.findAllUsersBookingByState(booker.getId(), BookingState.FUTURE, 0, 10);

        Assertions.assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldGetAllBookingIfStateIsWaiting() {
        List<BookingOutDto> result = service.findAllUsersBookingByState(booker.getId(), BookingState.WAITING, 0, 10);

        Assertions.assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldGetAllBookingIfStateIsRejected() {
        List<BookingOutDto> result = service.findAllUsersBookingByState(booker.getId(), BookingState.REJECTED,
                0, 10);

        Assertions.assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserIfStateIsAll() {
        List<BookingOutDto> list1 = service.findAllBookingsForItemsOfUser(owner.getId(), BookingState.ALL, 0, 10);

        Assertions.assertThat(list1).isNotEmpty().hasSize(4);
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserIfStateIsCurrent() {
        List<BookingOutDto> list1 = service.findAllBookingsForItemsOfUser(owner.getId(), BookingState.CURRENT, 0, 10);

        Assertions.assertThat(list1).isNotEmpty().hasSize(2);
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserIfStateIsPast() {
        List<BookingOutDto> list1 = service.findAllBookingsForItemsOfUser(owner.getId(), BookingState.PAST, 0, 10);

        Assertions.assertThat(list1).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserIfStateIsFuture() {
        List<BookingOutDto> list1 = service.findAllBookingsForItemsOfUser(owner.getId(), BookingState.FUTURE, 0, 10);

        Assertions.assertThat(list1).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserIfStateIsWaiting() {
        List<BookingOutDto> list1 = service.findAllBookingsForItemsOfUser(owner.getId(), BookingState.WAITING, 0, 10);

        Assertions.assertThat(list1).isNotEmpty().hasSize(1);
    }

    @Test
    void shouldFindAllBookingsForItemsOfUserIfStateIsRejected() {
        List<BookingOutDto> list1 = service.findAllBookingsForItemsOfUser(owner.getId(), BookingState.REJECTED, 0, 10);

        Assertions.assertThat(list1).isNotEmpty().hasSize(1);
    }
}
