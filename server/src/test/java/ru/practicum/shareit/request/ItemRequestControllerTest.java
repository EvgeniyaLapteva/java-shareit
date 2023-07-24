package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String URL = "/requests";

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService service;

    private ItemRequestDto itemRequestDto;

    private ItemRequestDtoWithItems itemRequestDtoWithItems;

    @BeforeEach
    void beforeEach() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("отвертка")
                .build();

        itemRequestDtoWithItems = ItemRequestDtoWithItems.builder()
                .id(1L)
                .description("отвертка")
                .build();
    }

    @SneakyThrows
    @Test
    void shouldCreateItemRequest() {
        when(service.createRequest(anyLong(), any()))
                .thenReturn(itemRequestDto);

        mvc.perform(post(URL)
                .header(HEADER, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(itemRequestDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemRequestDto.getId()), Long.class),
                        jsonPath("$.description", Matchers.is(itemRequestDto.getDescription()))
                );

        verify(service, times(1))
                .createRequest(anyLong(), any(ItemRequestDto.class));
    }

    @SneakyThrows
    @Test
    void shouldGetListOfRequestsByRequestorId() {
        when(service.getRequestDtoByRequestorId(anyLong()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(URL)
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        content().json("[]"));

        when(service.getRequestDtoByRequestorId(anyLong()))
                .thenReturn(List.of(itemRequestDtoWithItems));

        mvc.perform(get(URL)
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        content().json(mapper.writeValueAsString(List.of(itemRequestDtoWithItems)))
                );

        verify(service, times(2))
                .getRequestDtoByRequestorId(anyLong());
    }

    @SneakyThrows
    @Test
    void shouldGetAllRequests() {
        when(service.getAllRequestsPageable(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(URL + "/all")
                .header(HEADER, 1L)
                .param("from", "0")
                .param("size", "10"))
                .andExpectAll(
                        status().isOk(),
                        content().json("[]")
                );

        when(service.getAllRequestsPageable(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestDtoWithItems));

        mvc.perform(get(URL + "/all")
                .header(HEADER, 1L)
                .param("from", "0")
                        .param("size", "10"))
                .andExpectAll(
                        status().isOk(),
                        content().json(mapper.writeValueAsString(List.of(itemRequestDtoWithItems)))
                );
        verify(service, times(2))
                .getAllRequestsPageable(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldGetItemRequestById() {
        when(service.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestDtoWithItems);

        mvc.perform(get(URL + "/1")
                .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemRequestDtoWithItems.getId()), Long.class),
                        jsonPath("$.description", Matchers.is(itemRequestDtoWithItems.getDescription()))
                );
        verify(service, times(1))
                .getRequestById(anyLong(), anyLong());
    }

//    @SneakyThrows
//    @Test
//    void shouldCreateValidationExceptionWhenCreateWithEmptyDescription() {
//        when(service.createRequest(anyLong(), any()))
//                .thenReturn(itemRequestDto);
//
//        mvc.perform(post(URL)
//                        .header(HEADER, 1L)
//                        .content("{}")
//                        .characterEncoding(StandardCharsets.UTF_8)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpectAll(
//                        status().isBadRequest(),
//                        content().contentType(MediaType.APPLICATION_JSON),
//                        content().json(
//                                "{" +
//                                        "    \"description\": \"Описание запроса вещи не должно быть пустым\"" +
//                                        " }")
//                );
//        verify(service, never())
//                .createRequest(anyLong(), any());
//    }

//    @SneakyThrows
//    @Test
//    void shouldCreateValidationExceptionWhenGetAllRequestsWithWrongParam() {
//        mvc.perform(get(URL + "/all")
//                .header(HEADER, 1L)
//                .param("from", "-1")
//                .param("size", "1"))
//                .andExpect(status().isBadRequest());
//
//        mvc.perform(get(URL + "/all")
//                .header(HEADER, 1L)
//                .param("from", "1")
//                .param("size", "0"))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never())
//                .getRequestById(anyLong(), anyLong());
//    }
}