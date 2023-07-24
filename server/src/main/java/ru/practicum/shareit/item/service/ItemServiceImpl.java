package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.BookingAndCommentException;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItemDto(Long userId, ItemDto itemDto) {
        User owner = validateUser(userId);
        Item item = ItemMapper.toItem(itemDto, owner);
        item = itemRepository.save(item);
        log.info("Создали вещь id = {}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItemDto(Long userId, Long itemId, ItemDto itemDto) {
        Item item = validateUserAndItem(userId, itemId);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.info("Обновили вещь id = {}", itemId);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoWithBookingAndComments getItemDtoById(Long itemId, Long userId) {
        User user = validateUser(userId);
        Item item = validateItem(itemId);
        List<Booking> bookings = bookingRepository.findByItemId(itemId);
        List<Comment> comments = commentRepository.findByItemId(itemId);
        return ItemMapper.toItemDtoWBC(item, comments, bookings, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoWithBookingAndComments> getItemDtoByUserId(Long userId, int from, int size) {
        User user = validateUser(userId);
        PageRequest page = PageRequest.of(from / size, size);
        List<Item> itemsOfUser = itemRepository.findByOwnerId(userId, page);
        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, page);
        List<Comment> comments = commentRepository.findByItemIdIn(itemsOfUser.stream()
                .map(Item::getId).collect(Collectors.toList()));
        log.info("Получили список вещей пользователя id = {}", userId);
        return itemsOfUser.stream()
                .map(item -> ItemMapper.toItemDtoWBC(item, comments, bookings, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsDtoByTextRequest(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        log.info("Получили список вещей по текстовому запросу {}", text);
        PageRequest page = PageRequest.of(from / size, size);
        return itemRepository.searchByText(text.toUpperCase(), page).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public void deleteItemDto(Long userId, Long itemId) {
        validateUserAndItem(userId, itemId);
        itemRepository.deleteById(itemId);
        log.info("Удалили вещь id = {}", itemId);
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        User user = validateUser(userId);
        Item item = validateItem(itemId);
        Booking booking = bookingRepository
                .findTopByStatusNotLikeAndItemIdAndBookerIdOrderByEndAsc(BookingStatus.REJECTED, itemId, userId);
        Comment comment = CommentMapper.toComment(commentDto, user, item);
        if (booking == null) {
            log.info("Пользователь id = {} не бронировал вещь id = {}", userId, itemId);
            throw new BookingAndCommentException("Пользователь id = " + userId + " не бронировал вещь id = " + itemId);
        }
        if (comment.getCreated().isBefore(booking.getEnd())) {
            log.info("Аренда должна закончиться к моменту написания комментария");
            throw new BookingAndCommentException("Аренда должна закончиться к моменту написания комментария");
        }
        log.info("Пользователь id = {} добавил комментарий к вещи id = {}", userId, itemId);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Item validateUserAndItem(Long userId, Long itemId) {
        validateUser(userId);
        Item item = validateItem(itemId);
        Long ownerId = item.getOwner().getId();
        if (!Objects.equals(userId, ownerId)) {
            log.error("Вещь id = {} не принадлежит пользователю id = {}", itemId, userId);
            throw new ObjectNotFoundException("Вещь id = " + itemId + " не принадлежит пользователю id = " + userId);
        }
        return item;
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        "с id = " + userId + " не найден"));
    }

    private Item validateItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Вещь с id = " + itemId + "не найдена"));
    }
}
