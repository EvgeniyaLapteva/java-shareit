package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String URL = "/bookings";
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient client;

    private BookItemRequestDto bookingDto;

    @BeforeEach
    void beforeEach() {
        LocalDateTime now = LocalDateTime.now();

        bookingDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(now.plusHours(1))
                .end(now.plusDays(2))
                .build();
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWithWrongStartTime() {
        bookingDto.setStart(LocalDateTime.of(1989, 10, 10, 10, 10));

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        bookingDto.setStart(null);
        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWithWrongEndTime() {
        bookingDto.setEnd(LocalDateTime.of(1989, 10, 10, 10, 10));

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        bookingDto.setStart(null);
        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestCreateBookingWithoutItem() {
        bookingDto.setItemId(null);

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenFindAllUsersBookingByStateIfWrongFrom() {
        mvc.perform(get(URL)
                        .header(HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenFindAllUsersBookingByStateIfWrongSize() {
        mvc.perform(get(URL)
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenFindAllBookingsForItemsOfUserIfWrongFrom() {
        mvc.perform(get(URL + "/owner")
                        .header(HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenFindAllBookingsForItemsOfUserIfWrongSize() {
        mvc.perform(get(URL + "/owner")
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .findAllBookingsForItemsOfUser(anyLong(), any(), anyInt(), anyInt());
    }
}