package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest public class ItemIntegrationTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private ItemService service;

    private User owner;

    private User booker;

    private Item item;

    private Item secondItem;

    private Comment comment;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setName("Tom");
        owner.setEmail("tom@mail.ru");
        manager.persist(owner);

        booker = new User();
        booker.setName("Rick");
        booker.setEmail("rick@mail.ru");
        manager.persist(booker);

        item = new Item();
        item.setName("tool");
        item.setDescription("good");
        item.setAvailable(true);
        item.setOwner(owner);
        manager.persist(item);

        secondItem = new Item();
        secondItem.setName("anotherTool");
        secondItem.setDescription("very small");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner);
        manager.persist(secondItem);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(10));
        booking.setEnd(LocalDateTime.now().minusDays(5));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        manager.persist(booking);

        comment = new Comment();
        comment.setText("create");
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(booker);
        manager.persist(comment);
    }

    @Test
    void shouldCreateItem() {
        Long userId = owner.getId();
        ItemDto dto = ItemDto.builder()
                .name("knife")
                .description("good")
                .available(true)
                .build();

        ItemDto result = service.createItemDto(userId, dto);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("name", "knife")
                .hasFieldOrPropertyWithValue("description", "good")
                .hasFieldOrProperty("id");
    }

    @Test
    void shouldUpdateItem() {
        ItemDto dto = ItemMapper.toItemDto(item);
        dto.setName("new");
        dto.setDescription("alsoNew");
        dto.setAvailable(false);

        ItemDto result = service.updateItemDto(owner.getId(), item.getId(), dto);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("name", "new")
                .hasFieldOrPropertyWithValue("description", "alsoNew")
                .hasFieldOrPropertyWithValue("id", item.getId())
                .hasFieldOrPropertyWithValue("available", false);
    }

    @Test
    void shouldGetItemById() {
        ItemDtoWithBookingAndComments result = service.getItemDtoById(item.getId(), owner.getId());
        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("name", "tool")
                .hasFieldOrPropertyWithValue("description", "good")
                .hasFieldOrPropertyWithValue("comments", List.of(commentDto))
                .hasFieldOrPropertyWithValue("id", item.getId());
    }

    @Test
    void shouldGetItemsByUserId() {
        List<ItemDtoWithBookingAndComments> result = service.getItemDtoByUserId(owner.getId(), 0, 2);

        Assertions.assertThat(result).isNotEmpty()
                .hasSize(2);
        Assertions.assertThat(result.get(0).getName()).isEqualTo(item.getName());
        Assertions.assertThat(result.get(1).getName()).isEqualTo(secondItem.getName());
    }

    @Test
    void shouldGetItemsByText() {
        String text = "too";

        List<ItemDto> result = service.getItemsDtoByTextRequest(text, 0, 2);

        Assertions.assertThat(result).isNotEmpty().hasSize(2);
    }

    @Test
    void shouldCreateComment() {
        CommentDto dto = CommentDto.builder()
                .text("good tool")
                .build();

        CommentDto result = service.createComment(dto, booker.getId(), item.getId());

        Assertions.assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("text", "good tool")
                .hasFieldOrPropertyWithValue("authorName", booker.getName());
    }
}
