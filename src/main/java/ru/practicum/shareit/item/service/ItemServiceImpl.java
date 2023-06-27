package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.FieldsAreNotSpecifiedException;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dao.impl.ItemDaoInMemoryImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.impl.UserDaoInMemoryImpl;
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

    @Override
    public ItemDto createItemDto(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        "с id = " + userId + " не найден"));
        Item item = ItemMapper.toItem(itemDto, owner);
        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItemDto(Long userId, Long itemId, ItemDto itemDto) {
        Item item = validateUser(userId, itemId);
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                log.error("Поле name не должно быть пустым");
                throw new ValidationException("Поле name не должно быть пустым");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                log.error("Поле name не должно быть пустым");
                throw new ValidationException("Поле description не должно быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemDtoById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Вещь с id = " + itemId + "не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemDtoByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        "с id = " + userId + " не найден"));
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsDtoByTextRequest(String text) {
        if (text.equals("")) {
            return Collections.emptyList();
        }
        String textToLowerCase = text.toLowerCase();
        log.info("Получили список вещей по текстовому запросу {}", text);
        return itemRepository.searchByText(textToLowerCase).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public void deleteItemDto(Long userId, Long itemId) {
        validateUser(userId, itemId);
        itemRepository.delete(userId,itemId);
    }

    private Item validateUser(Long userId, Long itemId) {
        userRepository.findUserById(userId);
        Item item = itemRepository.getItemById(itemId);
        Long ownerId = item.getOwner().getId();
        if (!Objects.equals(userId, ownerId)) {
            log.error("Вещь id = {} не принадлежит пользователю id = {}", itemId, userId);
            throw new ObjectNotFoundException("Вещь id = " + itemId + " не принадлежит пользователю id = " + userId);
        }
        return item;
    }
}
