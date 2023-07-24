package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final String URL = "/users";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    private User user;

    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1L);
        user.setName("User");
        user.setEmail("user@mail.ru");

        userDto = UserMapper.toUserDto(user);
    }

    @SneakyThrows
    @Test
    void getAllUsers() {
        when(service.getAllUsers())
                .thenReturn(Collections.emptyList());

        mvc.perform(get(URL))
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$", hasSize(0))
               );

        when(service.getAllUsers())
                .thenReturn(List.of(userDto));
        mvc.perform(get(URL))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(userDto.getName())),
                        jsonPath("$[0].email", Matchers.is(userDto.getEmail()))
                );
        verify(service, times(2))
                .getAllUsers();
    }

    @SneakyThrows
    @Test
    void getUserById() {
        when(service.getUserById(anyLong()))
                .thenReturn(userDto);

        mvc.perform(get(URL + "/1"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(userDto.getName())),
                        jsonPath("$.email", Matchers.is(userDto.getEmail()))
                );
        verify(service, times(1))
                .getUserById(anyLong());
    }

    @SneakyThrows
    @Test
    void shouldCreateUser() {
        when(service.create(any()))
                .thenReturn(userDto);

        mvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(userDto.getName())),
                        jsonPath("$.email", Matchers.is(userDto.getEmail()))
                );
        verify(service, times(1))
                .create(any(UserDto.class));
    }

//    @Test
//    @SneakyThrows
//    void shouldGetStatusIsBadRequestWhenCreateUserWithEmptyName() {
//        UserDto badDto = UserDto.builder()
//                .name("")
//                .email("dring@mail.com")
//                .build();
//
//        mvc.perform(post(URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(badDto))
//                .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//
//        verify(service, never())
//                .create(any());
//    }

//    @Test
//    @SneakyThrows
//    void shouldGetStatusIsBadRequestWhenCreatedUserWithEmptyEmail() {
//        UserDto badDto = UserDto.builder()
//                .name("Tom")
//                .email("")
//                .build();
//
//        mvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(badDto))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .create(any());
//    }

//    @Test
//    @SneakyThrows
//    void shouldGetStatusIsBadRequestWhenCreateUserWithWrongEmail() {
//        UserDto badDto = UserDto.builder()
//                .name("Tom")
//                .email("tom")
//                .build();
//
//        mvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(badDto))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .create(any());
//    }

    @SneakyThrows
    @Test
    void shouldUpdateUserWithName() {
        when(service.update(anyLong(), any()))
                .thenReturn(userDto);

        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"User\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(userDto.getName())),
                        jsonPath("$.email", Matchers.is(userDto.getEmail()))
                );
        verify(service, times(1))
                .update(anyLong(), any(UserDto.class));
    }

    @SneakyThrows
    @Test
    void shouldUpdateUserWithEmail() {
        when(service.update(anyLong(), any()))
                .thenReturn(userDto);

        mvc.perform(patch(URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":  \"user@mail.ru\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(userDto.getName())),
                        jsonPath("$.email", Matchers.is(userDto.getEmail()))
                );
        verify(service, times(1))
                .update(anyLong(), any(UserDto.class));
    }

//    @SneakyThrows
//    @Test
//    void shouldGetStatusIsBadRequestWhenUpdateWithWrongEmail() {
//        mvc.perform(patch(URL + "/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"email\":  \"usermail.ru\"}")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//        verify(service, never())
//                .update(anyLong(), any());
//    }

    @SneakyThrows
    @Test
    void shouldDeleteUserById() {
        mvc.perform(delete(URL + "/1"))
                .andExpect(status().isOk());
        verify(service, times(1))
                .deleteUserById(anyLong());
    }
}