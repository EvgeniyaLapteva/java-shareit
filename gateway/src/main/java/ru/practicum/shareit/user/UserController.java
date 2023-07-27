package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.validation.ValidationMarker;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserClient client;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Запрос на получение списка всех пользователей");
        return client.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя по id = {}", id);
        return client.getUserById(id);
    }

    @PostMapping
    @Validated({ValidationMarker.OnCreate.class})
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос на создание пользователя: {}", userDto);
        return client.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        log.info("Запрос на обновление пользователя id = {}", id);
        return client.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя id = {}", id);
        return client.deleteUserById(id);
    }
}
