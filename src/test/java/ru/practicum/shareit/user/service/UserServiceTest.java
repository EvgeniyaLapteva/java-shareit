package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl service;

    private User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1L);
        user.setName("Markus");
        user.setEmail("markus@email.com");
    }

    @Test
    void shouldGetAllUsersWhenRepositoryIsEmpty() {
        when(repository.findAll())
                .thenReturn(Collections.emptyList());

        List<UserDto> userDtos = service.getAllUsers();

        assertNotNull(userDtos);
        assertEquals(0, userDtos.size());
        verify(repository, times(1))
                .findAll();
    }

    @Test
    void shouldGetAllUsersWhenUserExists() {
        when(repository.findAll())
                .thenReturn(List.of(user));

        List<UserDto> userDtos = service.getAllUsers();

        assertNotNull(userDtos);
        assertEquals(1, userDtos.size());
        verify(repository, times(1))
                .findAll();
    }

    @Test
    void shouldGetUserById() {
        Long userId = user.getId();
        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto result = service.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenFindByIdIfUserNotFound() {
        Long userId = 0L;
        String errorMessage = "Пользователь с id = " + userId + " не найден";
        when(repository.findById(userId))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.getUserById(userId)
        );

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldCreateUser() {
        User userForCreate = new User();
        userForCreate.setName("Markus");
        userForCreate.setEmail("markus@email.com");

        when(repository.save(any()))
                .thenReturn(user);
        UserDto dto = UserMapper.toUserDto(userForCreate);
        UserDto result = service.create(dto);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(repository, times(1))
                .save(any());
    }

    @Test
    void shouldUpdateNameWhenUpdateUser() {
        Long userId = user.getId();
        String newName = "New";
        User userForUpdate = new User();
        userForUpdate.setId(userId);
        userForUpdate.setName(newName);
        userForUpdate.setEmail(user.getEmail());
        when(repository.findById(userId))
                .thenReturn(Optional.of(user));
        when(repository.save(any()))
                .thenReturn(userForUpdate);

        UserDto result = service.update(userId, UserDto.builder().name(newName).build());

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(newName, result.getName());
    }

    @Test
    void shouldUpdateEmailWhenUpdateUser() {
        Long userId = user.getId();
        String newEmail = "new@mail.ru";
        User userForUpdate = new User();
        userForUpdate.setId(userId);
        userForUpdate.setName(user.getName());
        userForUpdate.setEmail(newEmail);
        when(repository.findById(userId))
                .thenReturn(Optional.of(user));
        when(repository.save(any()))
                .thenReturn(userForUpdate);

        UserDto result = service.update(userId, UserDto.builder().email(newEmail).build());

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(newEmail, result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUpdateUserThatDoesntExist() {
        Long userId = 0L;
        String errorMessage = "Пользователь" +
                "с id = " + userId + " не найден";
        when(repository.findById(userId))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.getUserById(userId));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateUserIfNameIsBlank() {
        Long userId = user.getId();
        String errorMessage = "Поле name не должно быть пустым";
        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.update(userId, UserDto.builder().name("").build()));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateUserIfEmailIsBlank() {
        Long userId = user.getId();
        String errorMessage = "Поле email не должно быть пустым";
        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.update(userId, UserDto.builder().email("").build()));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldDeleteUserById() {
        Long userId = 1L;

        service.deleteUserById(userId);

        verify(repository, times(1))
                .deleteById(userId);
    }
}