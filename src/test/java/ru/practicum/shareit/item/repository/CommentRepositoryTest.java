package ru.practicum.shareit.item.repository;

import org.assertj.core.api.Assertions;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager manager;

    @Autowired
    CommentRepository repository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    private User author;

    private User owner;

    private Item item;

    private Comment comment;

    @BeforeEach
    void beforeEach() {
        author = new User();
        author.setName("Marta");
        author.setEmail("marta@mail.com");
        manager.persist(author);

        owner = new User();
        owner.setName("Tom");
        owner.setEmail("tom@mail.com");
        manager.persist(owner);

        item = new Item();
        item.setName("tool");
        item.setDescription("nice");
        item.setAvailable(true);
        item.setOwner(owner);
        manager.persist(item);

        comment = new Comment();
        comment.setText("very nice tool");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        manager.persist(comment);
    }

    @Test
    public void contextLoads() {
        Assertions.assertThat(manager).isNotNull();
    }

    @Test
    void shouldFindByItemId() {
        List<Comment> result = repository.findByItemId(comment.getItem().getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment, result.get(0));
    }

    @Test
    void shouldFindByItemIdIn() {
        Item secondItem = new Item();
        secondItem.setOwner(owner);
        secondItem.setAvailable(true);
        secondItem.setName("ball");
        secondItem.setDescription("big");
        itemRepository.save(secondItem);

        Comment secondComment = new Comment();
        secondComment.setCreated(LocalDateTime.now());
        secondComment.setItem(secondItem);
        secondComment.setText("very big ball");
        secondComment.setAuthor(author);
        repository.save(secondComment);

        List<Comment> result = repository.findByItemIdIn(List.of(item.getId(), secondItem.getId()));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(comment, result.get(0));
        assertEquals(secondComment, result.get(1));
    }
}