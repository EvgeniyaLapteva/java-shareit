package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
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
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    @MockBean
    ItemRequestService service;

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("отвертка")
            .build();

    private final ItemRequestDtoWithItems itemRequestDtoWithItems = ItemRequestDtoWithItems.builder()
            .id(1L)
            .description("отвертка")
            .build();

    @Test
    void shouldCreateItemRequest() throws Exception {
        when(service.createRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mvc.perform(post(URL)
                .header("X-Sharer-User-Id", 1L)
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

    @Test
    void shouldGetListOfRequestsByRequestorId() throws Exception {
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

    @Test
    void shouldCreateValidationExceptionWhenCreateWithEmptyDescription() throws Exception {
        when(service.createRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .content("{}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                "{" +
                                        "    \"description\": \"Описание запроса вещи не должно быть пустым\"" +
                                        " }")
                );
    }


}