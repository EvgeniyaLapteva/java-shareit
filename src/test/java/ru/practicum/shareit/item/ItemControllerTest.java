package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService service;

    private ItemDto itemDto;

    private ItemDtoWithBookingAndComments fullItemDto;

    private BookingDto bookingDto;

    private CommentDto commentDto;

    @BeforeEach
    void beforeEach() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("knife")
                .description("thin")
                .available(true)
                .build();

        bookingDto = BookingDto.builder().build();

        fullItemDto = ItemDtoWithBookingAndComments.builder()
                .id(2L)
                .name("knife")
                .description("thin")
                .available(true)
                .lastBooking(bookingDto)
                .nextBooking(bookingDto)
                .comments(Collections.emptyList())
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("good knife")
                .authorName("Tom")
                .itemId(2L)
                .build();
    }

    @Test
    void createItem() {
    }

    @Test
    void updateItem() {
    }

    @Test
    void getItemById() {
    }

    @Test
    void getItemsByUserId() {
    }

    @Test
    void getItemsByTextRequest() {
    }

    @Test
    void deleteItem() {
    }

    @Test
    void createComment() {
    }
}