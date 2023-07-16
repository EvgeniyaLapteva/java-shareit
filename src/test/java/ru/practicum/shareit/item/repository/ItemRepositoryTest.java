package ru.practicum.shareit.item.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {

    private static final String QUERY = " select i from Item i " +
            "where upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%')) and i.available = true";

    @Autowired
    private TestEntityManager manager;

    @Autowired
    private ItemRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;

    private User requestor;

    private ItemRequest request;

    private ItemRequest secondRequest;

    private Item item;

    private Item secondItem;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setName("Tom");
        owner.setEmail("tom@mail.com");
        manager.persist(owner);

        requestor = new User();
        requestor.setName("Marta");
        requestor.setEmail("marta@mail.com");
        manager.persist(requestor);

        request = new ItemRequest();
        request.setDescription("nice tool");
        request.setRequestor(requestor);
        manager.persist(request);

        secondRequest = new ItemRequest();
        secondRequest.setDescription("need bike");
        secondRequest.setRequestor(requestor);
        manager.persist(secondRequest);

        item = new Item();
        item.setName("tool");
        item.setDescription("nice");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        manager.persist(item);

        secondItem = new Item();
        secondItem.setName("bike");
        secondItem.setDescription("load and nice");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner);
        secondItem.setRequest(secondRequest);
        manager.persist(secondItem);
    }

    @Test
    public void contextLoads() {
        Assertions.assertThat(manager).isNotNull();
    }

    @Test
    void shouldFindItemByOwnerId() {
        List<Item> result = repository.findByOwnerId(owner.getId());

        Assertions.assertThat(result).isNotNull().hasSize(2);
    }

    @Test
    void shouldFindItemByOwnerIdPageable() {
        int from = 0;
        int size = 1;
        int secondSize = 2;
        PageRequest page = PageRequest.of(from, size);
        PageRequest pageForTwo = PageRequest.of(from, secondSize);

        List<Item> result = repository.findByOwnerId(owner.getId(), page);
        List<Item> resultIsTwo = repository.findByOwnerId(owner.getId(), pageForTwo);

        Assertions.assertThat(result).isNotNull().hasSize(1);
        Assertions.assertThat(resultIsTwo).isNotNull().hasSize(2);
    }

    @Test
    void ShouldSearchByTextWithoutMatches() {
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from, size);
        String text = "quest";
        TypedQuery<Item> query = manager.getEntityManager()
                .createQuery(QUERY, Item.class);

        List<Item> items = query.setParameter(1, text).getResultList();
        List<Item>itemsFromRepository = repository.searchByText(text, page);

        assertNotNull(items);
        assertEquals(0, items.size());
        assertNotNull(itemsFromRepository);
        assertEquals(0, itemsFromRepository.size());
    }

    @Test
    void ShouldSearchByTextWithMatches() {
        int from = 0;
        int size = 2;
        PageRequest page = PageRequest.of(from, size);
        String text = "too";
        String alsoText = "nice";
        TypedQuery<Item> query = manager.getEntityManager()
                .createQuery(QUERY, Item.class);

        List<Item> items = query.setParameter(1, text).getResultList();
        List<Item> itemsFromRepository = repository.searchByText(text, page);
        List<Item> twoItems = query.setParameter(1, alsoText).getResultList();
        List<Item> twoItemFromRepo = repository.searchByText(alsoText, page);


        assertNotNull(items);
        assertEquals(1, items.size());
        assertNotNull(itemsFromRepository);
        assertEquals(1, itemsFromRepository.size());
        assertNotNull(twoItems);
        assertEquals(2, twoItems.size());
        assertNotNull(twoItemFromRepo);
        assertEquals(2, twoItemFromRepo.size());
    }

    @Test
    void shouldFindByRequestId() {
        List<Item> result = repository.findByRequestId(request.getId());

        Assertions.assertThat(result).isNotNull().hasSize(1)
                .hasAtLeastOneElementOfType(Item.class);
    }

    @Test
    void findByRequestIdIn() {
        List<Item> result = repository.findByRequestIdIn(List.of(request.getId(), secondRequest.getId()));

        Assertions.assertThat(result).isNotNull().hasSize(2)
                .hasAtLeastOneElementOfType(Item.class);
    }
}