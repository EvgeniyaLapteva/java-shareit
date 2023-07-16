package ru.practicum.shareit.request.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class ItemRequestRepositoryTest {

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Autowired
    private TestEntityManager manager;

    @Autowired
    private ItemRequestRepository repository;

    private User requestor;

    private User secondUser;

    private ItemRequest request;

    private ItemRequest secondRequest;

    @BeforeEach
    void beforeEach() {
        requestor = new User();
        requestor.setName("Marta");
        requestor.setEmail("marta@mail.com");
        manager.persist(requestor);

        secondUser = new User();
        secondUser.setName("Tom");
        secondUser.setEmail("tom@mail.com");
        manager.persist(secondUser);

        request = new ItemRequest();
        request.setDescription("need tool");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        manager.persist(request);

        secondRequest = new ItemRequest();
        secondRequest.setRequestor(secondUser);
        secondRequest.setDescription("ball");
        secondRequest.setCreated(LocalDateTime.now());
        manager.persist(secondRequest);
    }

    @Test
    public void contextLoads() {
        Assertions.assertThat(manager).isNotNull();
    }

    @Test
    void shouldFindByRequestorId() {
        List<ItemRequest> result = repository.findByRequestorId(requestor.getId());

        Assertions.assertThat(result).isNotNull().hasSize(1).hasAtLeastOneElementOfType(ItemRequest.class);
    }

    @Test
    void findAllByRequestorIdNot() {
        int from = 0;
        int size = 2;
        PageRequest page = PageRequest.of(from, size, SORT);
        List<ItemRequest> result = repository.findAllByRequestorIdNot(secondUser.getId(), page);

        Assertions.assertThat(result).isNotNull().hasSize(1).hasAtLeastOneElementOfType(ItemRequest.class);
    }
}