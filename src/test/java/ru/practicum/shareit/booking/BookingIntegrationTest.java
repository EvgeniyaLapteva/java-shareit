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
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

@Transactional
@SpringBootTest
public class BookingIntegrationTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private BookingService service;

    private final LocalDateTime NOW = LocalDateTime.now();

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
        bookingJustSaved.setEnd(NOW.minusDays(5));
        manager.persist(bookingJustSaved);

        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setStart(NOW.minusDays(10));
        booking.setEnd(NOW.minusDays(6));
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

        BookingOutDto result = service.create(booking.getId(), dto);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        Assertions.assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        Assertions.assertThat(result.getItem().getId()).isEqualTo(item.getId());
    }
}
