package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto getUserById(Long userId);
    UserDto create(UserDto userDto);
    UserDto update(Long userDtoId, UserDto userDto);
    void deleteUserById(Long userDtoId);

}
