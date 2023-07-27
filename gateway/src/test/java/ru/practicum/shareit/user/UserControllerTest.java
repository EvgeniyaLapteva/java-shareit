package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final String URL = "/users";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserClient client;

    @Test
    @SneakyThrows
    void shouldGetStatusIsBadRequestWhenCreateUserWithEmptyName() {
        UserDto badDto = UserDto.builder()
                .name("")
                .email("dring@mail.com")
                .build();

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(badDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(client, never())
                .createUser(any());
    }

    @Test
    @SneakyThrows
    void shouldGetStatusIsBadRequestWhenCreatedUserWithEmptyEmail() {
        UserDto badDto = UserDto.builder()
                .name("Tom")
                .email("")
                .build();

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(badDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(client, never())
                .createUser(any());
    }

    @Test
    @SneakyThrows
    void shouldGetStatusIsBadRequestWhenCreateUserWithWrongEmail() {
        UserDto badDto = UserDto.builder()
                .name("Tom")
                .email("tom")
                .build();

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(badDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(client, never())
                .createUser(any());
    }

    @SneakyThrows
    @Test
    void shouldGetStatusIsBadRequestWhenUpdateWithWrongEmail() {
        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":  \"usermail.ru\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(client, never())
                .updateUser(anyLong(), any());
    }

}