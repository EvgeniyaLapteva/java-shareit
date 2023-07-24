package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String URL = "/items";

    private static final String HEADER = "X-Sharer-User-Id";

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

    @SneakyThrows
    @Test
    void shouldCreateItem() {
        when(service.createItemDto(anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(post(URL)
                .header(HEADER, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(itemDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDto.getName())),
                        jsonPath("$.description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDto.getAvailable()))
                );
        verify(service, times(1))
                .createItemDto(anyLong(), any());
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenCreateItemIfNameIsBlank() {
//        itemDto.setName(null);
//
//        mvc.perform(post(URL)
//                .header(HEADER, 1L)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(itemDto)))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .createItemDto(anyLong(), any());
//    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenCreateItemIfDescriptionIsBlank() {
//        itemDto.setDescription(null);
//
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(itemDto)))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .createItemDto(anyLong(), any());
//    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenCreateItemIfAvailableIsBlank() {
//        itemDto.setAvailable(null);
//
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(itemDto)))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .createItemDto(anyLong(), any());
//    }

    @SneakyThrows
    @Test
    void shouldUpdateItemWithName() {
        itemDto.setName("tool");
        when(service.updateItemDto(anyLong(), anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(patch(URL + "/1")
                .header(HEADER, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"tool\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is("tool"))
                );
        verify(service, times(1))
                .updateItemDto(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldUpdateItemWithDescription() {
        itemDto.setDescription("unique");
        when(service.updateItemDto(anyLong(), anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(patch(URL + "/1")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"unique\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.description", Matchers.is("unique"))
                );
        verify(service, times(1))
                .updateItemDto(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldUpdateItemWithAvailable() {
        itemDto.setAvailable(false);
        when(service.updateItemDto(anyLong(), anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(patch(URL + "/1")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": \"false\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.available", Matchers.is(itemDto.getAvailable()))
                );
        verify(service, times(1))
                .updateItemDto(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetItemById() {
        when(service.getItemDtoById(anyLong(), anyLong()))
                .thenReturn(fullItemDto);

        mvc.perform(get(URL + "/1")
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(fullItemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(fullItemDto.getName())),
                        jsonPath("$.description", Matchers.is(fullItemDto.getDescription()))
                );
        verify(service, times(1))
                .getItemDtoById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void shouldGetItemsByUserId() {
        when(service.getItemDtoByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(fullItemDto));

        mvc.perform(get(URL)
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(fullItemDto.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(fullItemDto.getName())),
                        jsonPath("$[0].description", Matchers.is(fullItemDto.getDescription())),
                        jsonPath("$[0].available", Matchers.is(fullItemDto.getAvailable())),
                        jsonPath("$.length()", Matchers.is(1))
                );
        verify(service, times(1))
                .getItemDtoByUserId(anyLong(), anyInt(), anyInt());
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenGetItemByUserIdWithWrongGParam() {
//        when(service.getItemDtoByUserId(anyLong(), anyInt(), anyInt()))
//                .thenReturn(List.of(fullItemDto));
//
//        mvc.perform(get(URL)
//                        .header(HEADER, 1L)
//                        .param("from", "-1")
//                        .param("size", "1"))
//                .andExpect(status().isBadRequest());
//
//        mvc.perform(get(URL)
//                        .header(HEADER, 1L)
//                        .param("from", "1")
//                        .param("size", "0"))
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .getItemDtoByUserId(anyLong(), anyInt(), anyInt());
//    }

    @SneakyThrows
    @Test
    void shouldGetItemsByTextRequest() {
        when(service.getItemsDtoByTextRequest(anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        mvc.perform(get(URL + "/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        when(service.getItemsDtoByTextRequest(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));
        mvc.perform(get(URL + "/search")
                        .param("text", "kn")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));
        verify(service, times(2))
                .getItemsDtoByTextRequest(anyString(), anyInt(), anyInt());
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenGetItemsByTextRequestWithWrongParam() {
//        when(service.getItemsDtoByTextRequest(anyString(), anyInt(), anyInt()))
//                .thenReturn(List.of(itemDto));
//
//        mvc.perform(get(URL + "/search")
//                        .param("text", "")
//                        .param("from", "-1")
//                        .param("size", "1"))
//                .andExpect(status().isBadRequest());
//
//        mvc.perform(get(URL + "/search")
//                        .param("text", "")
//                        .param("from", "1")
//                        .param("size", "0"))
//                .andExpect(
//                        status().isBadRequest());
//        verify(service, never())
//                .getItemsDtoByTextRequest(anyString(), anyInt(), anyInt());
//    }

    @SneakyThrows
    @Test
    void shouldDeleteItem() {
        mvc.perform(delete(URL + "/1")
                        .header(HEADER,  1L))
                .andExpect(status().isOk());

        verify(service, times(1))
                .deleteItemDto(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void createComment() {
        when(service.createComment(any(), anyLong(), anyLong())).thenReturn(commentDto);

        mvc.perform(post(URL + "/1/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }
}