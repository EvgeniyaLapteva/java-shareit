package ru.practicum.shareit.user.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.model.EmailDoesNotExistException;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class UserDaoInMemoryImpl implements UserDao {

    private Long id = 0L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>(users.values());
        return allUsers;
    }

    @Override
    public User findUserById(Long userId) {
        validateUserById(userId);
        return users.get(userId);
    }

    @Override
    public User createUser(User user) {
        //validateUser(user);
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUserById(Long userId) {
        validateUserById(userId);
        users.remove(userId);
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    private Long generateId() {
        return ++id;
    }

//    private void validateUser(User user) {
//        if (user.getEmail() == null || !user.getEmail().contains("@")) {
//            log.error("email равен null или указан неверный формат");
//            throw new EmailDoesNotExistException("email равен null или указан неверный формат");
//        }
//        List<User> usersFromMemory = getAllUsers();
//        for (User userFM : usersFromMemory) {
//            if (user.getEmail().equals(userFM.getEmail())) {
//                log.error("Пользователь с email = {} уже существует", user.getEmail());
//                throw new ValidationException("Пользователь с email = " + user.getEmail() + "уже существует");
//            }
//        }
//    }

    private void validateUserById(Long userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}