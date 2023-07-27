package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Получили список всех пользователей");
        return repository.findAll()
                .stream().map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с id = " + userId + " не найден"));
        log.info("Получили пользователя по id = {}", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User userFromDB = new User();
        try {
            userFromDB = repository.save(user);
            log.info("Создан пользователь {}", userFromDB);
        } catch (RuntimeException exception) {
            if (exception.getMessage().contains("uq_user_email")) {
                throw new ValidationException("Пользователь с email = " + user.getEmail() + " уже зарегистрирован");
            }
        }
        return UserMapper.toUserDto(userFromDB);
    }

    @Override
    public UserDto update(Long userDtoId, UserDto userDto) {
        User user = repository.findById(userDtoId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        "с id = " + userDtoId + " не найден"));
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
            user.setEmail(userDto.getEmail());
        }
        log.info("Обновили пользователя с id = {}", userDtoId);
        return UserMapper.toUserDto(repository.save(user));
    }

    @Override
    public void deleteUserById(Long userDtoId) {
        repository.deleteById(userDtoId);
        log.info("Удалили пользователя по id = {}", userDtoId);
    }
}
