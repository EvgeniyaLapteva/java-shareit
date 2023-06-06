package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserDao {

    List<User> getAllUsers();
    User findUserById(Long userId);
    User createUser(User user);
    void deleteUserById(Long userId);
    User updateUser(User user);
}
