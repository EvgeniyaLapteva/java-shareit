package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String URL = "/items";

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient client;

    private ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("knife")
                .description("thin")
                .available(true)
                .build();
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenCreateItemIfNameIsBlank() {
        itemDto.setName(null);

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .createItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenCreateItemIfDescriptionIsBlank() {
        itemDto.setDescription(null);

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .createItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenCreateItemIfAvailableIsBlank() {
        itemDto.setAvailable(null);

        mvc.perform(post(URL)
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .createItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenGetItemByUserIdWithWrongGParam() {
        mvc.perform(get(URL)
                        .header(HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest());

        mvc.perform(get(URL)
                        .header(HEADER, 1L)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
        verify(client, never())
                .getItemsByUserId(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenGetItemsByTextRequestWithWrongParam() {

        mvc.perform(get(URL + "/search")
                        .header(HEADER, 1L)
                        .param("text", "")
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest());

        mvc.perform(get(URL + "/search")
                        .header(HEADER, 1L)
                        .param("text", "")
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(
                        status().isBadRequest());
        verify(client, never())
                .getItemsByTextRequest(anyString(), anyInt(), anyInt(), anyLong());
    }
}