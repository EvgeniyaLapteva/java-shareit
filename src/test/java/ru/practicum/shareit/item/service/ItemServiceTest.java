package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.BookingAndCommentException;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private ItemRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl service;

    private User owner;

    private User booker;

    private Item item;

    private Booking booking;

    private Comment comment;

    @BeforeEach
    void beforeEach() {
        LocalDateTime start = NOW.minusDays(5);
        LocalDateTime end = NOW.minusDays(1);

        owner = new User();
        owner.setId(1L);
        owner.setName("Tom");
        owner.setEmail("tom@mail.ru");

        booker = new User();
        booker.setId(2L);
        booker.setName("Rick");
        booker.setEmail("rick@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("tool");
        item.setDescription("load");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("good tool");
        comment.setAuthor(booker);
        comment.setItem(item);
        comment.setCreated(NOW);
    }

    @Test
    void shouldCreateItemDto() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.save(any()))
                .thenReturn(item);
        ItemDto itemDto = ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();

        ItemDto result = service.createItemDto(owner.getId(), itemDto);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        verify(repository, times(1))
                .save(any());
    }

    @Test
    void shouldUpdateItemDto() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(repository.save(any()))
                .thenReturn(item);
        ItemDto itemDto = ItemDto.builder()
                .name("old")
                .description("old")
                .available(false)
                .build();

        ItemDto result = service.updateItemDto(owner.getId(), item.getId(), itemDto);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        verify(repository, times(1))
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateItemWithEmptyName() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        String errorMessage = "Поле name не должно быть пустым";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateItemDto(owner.getId(), item.getId(), ItemDto.builder().name("").build()));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateItemWithEmptyDescription() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        String errorMessage = "Поле description не должно быть пустым";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateItemDto(owner.getId(), item.getId(), ItemDto.builder().description("").build()));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldGetItemDtoById() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(anyLong()))
                .thenReturn(List.of(booking));
        when(commentRepository.findByItemId(anyLong()))
                .thenReturn(List.of(comment));

        ItemDtoWithBookingAndComments result = service.getItemDtoById(owner.getId(), item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(comment.getId(), result.getComments().get(0).getId());
        verify(repository, times(1))
                .findById(anyLong());
    }

    @Test
    void shouldGetItemDtoByUserId() {
        int from = 0;
        int size = 1;
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        PageRequest page = PageRequest.of(from / size, size);
        when(repository.findByOwnerId(owner.getId(), page))
                .thenReturn(List.of(item));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(owner.getId(), page))
                .thenReturn(List.of(booking));
        when(commentRepository.findByItemIdIn(any()))
                .thenReturn(List.of(comment));

        List<ItemDtoWithBookingAndComments> result = service.getItemDtoByUserId(owner.getId(), from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment.getId(), result.get(0).getComments().get(0).getId());
        assertEquals(booking.getId(), result.get(0).getLastBooking().getId());
        verify(repository, times(1))
                .findByOwnerId(anyLong(), any());
    }

    @Test
    void shouldGetItemsDtoByTextRequestWithEmptyText() {
        int from = 0;
        int size = 1;
        String text ="";

        List<ItemDto> result = service.getItemsDtoByTextRequest(text, from, size);

        assertNotNull(result);
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void shouldGetItemsDtoByTextRequest() {
        int from = 0;
        int size = 1;
        String text ="too";
        PageRequest page = PageRequest.of(from / size, size);
        when(repository.searchByText(text.toUpperCase(), page))
                .thenReturn(List.of(item));

        List<ItemDto> result = service.getItemsDtoByTextRequest(text, from, size);

        assertNotNull(result);
        assertEquals(item.getId(), result.get(0).getId());
        assertEquals(1, result.size());
        verify(repository, times(1))
                .searchByText(any(), any());
    }

    @Test
    void shouldDeleteItemDto() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        service.deleteItemDto(owner.getId(), item.getId());

        verify(repository, times(1))
                .deleteById(anyLong());
    }

    @Test
    void shouldThroeExceptionWhenDeleteItemByIdWithoutOwner() {
        Long userId = booker.getId();
        Long itemId = item.getId();
        String errorMessage = "Вещь id = " + itemId + " не принадлежит пользователю id = " + userId;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.deleteItemDto(userId, itemId));

        assertEquals(errorMessage, exception.getMessage());
        verify(repository, never())
                .deleteById(anyLong());
    }

    @Test
    void shouldCreateComment() {
        Long userId = booker.getId();
        Long itemId = item.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository
                .findTopByStatusNotLikeAndItemIdAndBookerIdOrderByEndAsc(BookingStatus.REJECTED, itemId, userId))
                .thenReturn(booking);
        when(commentRepository.save(any()))
                .thenReturn(comment);

        CommentDto result = service.createComment(CommentMapper.toCommentDto(comment), userId, itemId);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        verify(commentRepository, times(1))
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateCommentIfUserIsNotBooker() {
        Long itemId = item.getId();
        Long userId = owner.getId();
        String errorMessage = "Пользователь id = " + userId + " не бронировал вещь id = " + itemId;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(owner));
        when(repository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingRepository
                .findTopByStatusNotLikeAndItemIdAndBookerIdOrderByEndAsc(BookingStatus.REJECTED, itemId, userId))
                .thenReturn(null);

        BookingAndCommentException exception = assertThrows(BookingAndCommentException.class,
                () -> service.createComment(CommentMapper.toCommentDto(comment), userId, itemId));
        assertEquals(errorMessage, exception.getMessage());
        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreateCommentIfBookingIsNotOver() {
        String errorMessage = "Аренда должна закончиться к моменту написания комментария";
        booking.setEnd(NOW.plusDays(10));
        Long userId = booker.getId();
        Long itemId = item.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(repository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository
                .findTopByStatusNotLikeAndItemIdAndBookerIdOrderByEndAsc(BookingStatus.REJECTED, itemId, userId))
                .thenReturn(booking);

        BookingAndCommentException exception = assertThrows(BookingAndCommentException.class,
                () -> service.createComment(CommentMapper.toCommentDto(comment), userId, itemId));

        assertEquals(errorMessage, exception.getMessage());
        verify(commentRepository, never())
                .save(any());
    }
}