package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String URL = "/bookings";
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService service;

    private BookingDto bookingDto;

    private BookingOutDto bookingOutDto;

    @BeforeEach
    void beforeEach() {
        LocalDateTime now = LocalDateTime.now();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Tom")
                .email("tom@mail.ru")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("knife")
                .description("big")
                .available(true)
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(now.plusHours(1))
                .end(now.plusDays(2))
                .bookerId(1L)
                .build();

        bookingOutDto = BookingOutDto.builder()
                .id(1L)
                .item(itemDto)
                .start(now.plusHours(1))
                .end(now.plusDays(2))
                .booker(userDto)
                .build();
    }

    @SneakyThrows
    @Test
    void shouldCreateBooking() {
        when(service.create(anyLong(), any()))
                .thenReturn(bookingOutDto);

        mvc.perform(post(URL)
                .header(HEADER, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bookingDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutDto.getBooker().getId()), Long.class)
                );
        verify(service, times(1))
                .create(anyLong(), any());
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWithWrongStartTime() {
//        bookingDto.setStart(LocalDateTime.of(1989, 10, 10, 10, 10));
//
//        mvc.perform(post(URL)
//                .header(HEADER, 1L)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(bookingDto)))
//                .andExpect(status().isBadRequest());
//
//        bookingDto.setStart(null);
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(bookingDto)))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .create(anyLong(), any());
//    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWithWrongEndTime() {
//        bookingDto.setEnd(LocalDateTime.of(1989, 10, 10, 10, 10));
//
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(bookingDto)))
//                .andExpect(status().isBadRequest());
//
//
//
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(bookingDto)))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .create(anyLong(), any());
//    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestCreateBookingWithoutItem() {
//        bookingDto.setItemId(null);
//
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(bookingDto)))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .create(anyLong(), any());
//    }

    @SneakyThrows
    @Test
    void shouldUpdateBookingStatusByOwnerWhenApproved() {
        bookingOutDto.setStatus(BookingStatus.APPROVED);
        when(service.updateBookingStatusByOwner(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingOutDto);

        mvc.perform(patch(URL + "/1")
                .header(HEADER, 1L)
                .param("approved", "true"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutDto.getStatus().toString()))
                );
        verify(service, times(1))
                .updateBookingStatusByOwner(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void shouldUpdateBookingStatusByOwnerWhenRejected() {
        bookingOutDto.setStatus(BookingStatus.REJECTED);
        when(service.updateBookingStatusByOwner(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingOutDto);

        mvc.perform(patch(URL + "/1")
                        .header(HEADER, 1L)
                        .param("approved", "false"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutDto.getStatus().toString()))
                );
        verify(service, times(1))
                .updateBookingStatusByOwner(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void shouldFindBookingByBookingId() {
        when(service.findByBookingId(anyLong(), anyLong()))
                .thenReturn(bookingOutDto);

        mvc.perform(get(URL + "/1")
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutDto.getBooker().getId()), Long.class)
                );
        verify(service, times(1))
                .findByBookingId(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void shouldFindAllUsersBookingByState() {
        when(service.findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get(URL)
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(bookingOutDto.getId()), Long.class),
                        jsonPath("$[0].item.id", Matchers.is(bookingOutDto.getItem().getId()), Long.class),
                        jsonPath("$[0].booker.id", Matchers.is(bookingOutDto.getBooker().getId()), Long.class)
                );
        verify(service, times(1))
                .findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldFindAllUsersBookingByStateWhenBookingsDoesNotExists() {
        when(service.findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(URL)
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        content().json("[]")
                );
        verify(service, times(1))
                .findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt());
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenFindAllUsersBookingByStateIfWrongFrom() {
//        when(service.findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt()))
//                .thenReturn(Collections.emptyList());
//        mvc.perform(get(URL)
//                .header(HEADER, 1L)
//                .param("from", "-1")
//                .param("size", "1"))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt());
//    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenFindAllUsersBookingByStateIfWrongSize() {
//        when(service.findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt()))
//                .thenReturn(Collections.emptyList());
//        mvc.perform(get(URL)
//                        .header(HEADER, 1L)
//                        .param("from", "0")
//                        .param("size", "0"))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .findAllUsersBookingByState(anyLong(), any(), anyInt(), anyInt());
//    }

    @SneakyThrows
    @Test
    void shouldFindAllBookingsForItemsOfUser() {
        when(service.findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get(URL + "/owner")
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(bookingOutDto.getId()), Long.class),
                        jsonPath("$[0].item.id", Matchers.is(bookingOutDto.getItem().getId()), Long.class),
                        jsonPath("$[0].booker.id", Matchers.is(bookingOutDto.getBooker().getId()), Long.class)
                );
        verify(service, times(1))
                .findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldFindAllBookingsForItemsOfUserWhenBookingDoesntExist() {
        when(service.findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(URL + "/owner")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        content().json("[]")
                );
        verify(service, times(1))
                .findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt());
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenFindAllBookingsForItemsOfUserIfWrongFrom() {
//        when(service.findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt()))
//                .thenReturn(Collections.emptyList());
//        mvc.perform(get(URL + "/owner")
//                        .header(HEADER, 1L)
//                        .param("from", "-1")
//                        .param("size", "1"))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt());
//    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenFindAllBookingsForItemsOfUserIfWrongSize() {
        when(service.findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        mvc.perform(get(URL + "/owner")
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
        verify(service, never())
                .findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt());
    }

}