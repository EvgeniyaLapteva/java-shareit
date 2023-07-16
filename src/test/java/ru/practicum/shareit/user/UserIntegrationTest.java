package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Transactional
@SpringBootTest
public class UserIntegrationTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService service;

    private User user;

    private User secondUser;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("User");
        user.setEmail("email@mail.ru");
        manager.persist(user);

        secondUser = new User();
        secondUser.setName("second");
        secondUser.setEmail("mail@mail.com");
        manager.persist(secondUser);
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = UserDto.builder().name("Name").email("name@mail.com").build();

        UserDto result = service.create(userDto);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("name", "Name")
                .hasFieldOrPropertyWithValue("email", "name@mail.com")
                .hasFieldOrProperty("id")
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void shouldFindUserById() {
        Long userId = user.getId();

        UserDto result = service.getUserById(userId);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("name", "User")
                .hasFieldOrPropertyWithValue("email", "email@mail.ru")
                .hasFieldOrPropertyWithValue("id", userId)
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void shouldFindAllUsers() {
        List<UserDto> userDtos = service.getAllUsers();

        Assertions.assertThat(userDtos).isNotEmpty()
                .hasSize(2);
    }

    @Test
    void shouldDeleteUserById() {
        Long userId = user.getId();

        Long beforeDelete = repository.count();

        Assertions.assertThat(beforeDelete).isEqualTo(2);

        service.deleteUserById(userId);

        Long afterDelete = repository.count();
        Assertions.assertThat(afterDelete).isEqualTo(1);

        Optional<User> userOptional = repository.findById(userId);
        Assertions.assertThat(userOptional).isNotPresent();
    }
}
