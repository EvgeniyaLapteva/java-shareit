package ru.practicum.shareit.request;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
class RequestControllerTest {

    private static final String URL = "/requests";

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RequestClient client;

    @SneakyThrows
    @Test
    void shouldCreateValidationExceptionWhenCreateWithEmptyDescription() {
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
        verify(client, never())
                .createRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void shouldCreateValidationExceptionWhenGetAllRequestsWithWrongParam() {
        mvc.perform(get(URL + "/all")
                        .header(HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest());

        mvc.perform(get(URL + "/all")
                        .header(HEADER, 1L)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(client, never())
                .getRequestById(anyLong(), anyLong());
    }
}