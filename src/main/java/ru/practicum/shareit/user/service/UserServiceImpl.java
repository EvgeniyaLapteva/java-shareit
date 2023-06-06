package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.model.EmailDoesNotExistException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userRepository;
    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получили список всех пользователей");
        return userRepository.getAllUsers()
                .stream().map(UserMapper :: toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userRepository.findUserById(userId));
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateUser(userDto);
        log.info("Создан пользователь {}", userDto);
        return UserMapper.toUserDto(userRepository.createUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(Long userDtoId, UserDto userDto) {
        User user = userRepository.findUserById(userDtoId);
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                log.error("Поле name не должно быть пустым");
                throw new ValidationException("Поле name не должно быть пустым");
            }
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                log.error("Поле email не должно быть пустым");
                throw new ValidationException("Поле email не должно быть пустым");
            }
            if (!user.getEmail().equals(userDto.getEmail())) {
                validateUser(userDto);
            }
            user.setEmail(userDto.getEmail());
        }
        log.info("Обновили пользователя с id = {}", userDtoId);
        return UserMapper.toUserDto(userRepository.updateUser(user));
    }

    @Override
    public void deleteUserById(Long userDtoId) {
        userRepository.deleteUserById(userDtoId);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getEmail() == null || !userDto.getEmail().contains("@")) {
            log.error("email равен null или указан неверный формат");
            throw new EmailDoesNotExistException("email равен null или указан неверный формат");
        }
        List<UserDto> usersFromMemory = getAllUsers();
        for (UserDto userFM : usersFromMemory) {
            if (userDto.getEmail().equals(userFM.getEmail())) {
                log.error("Пользователь с email = {} уже существует", userDto.getEmail());
                throw new ValidationException("Пользователь с email = " + userDto.getEmail() + " уже существует");
            }
        }
    }
}
