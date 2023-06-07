package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.model.FieldsAreNotSpecifiedException;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dao.impl.ItemDaoInMemoryImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.impl.UserDaoInMemoryImpl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemDaoInMemoryImpl itemRepository;
    private final UserDaoInMemoryImpl userRepository;

    @Override
    public ItemDto createItemDto(Long userId, ItemDto itemDto) {
        userRepository.findUserById(userId);
        if (itemDto.getAvailable() == null) {
            log.error("Поле available должно быть указано");
            throw new FieldsAreNotSpecifiedException("Поле available должно быть указано");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.error("Поле name не должно быть пустым");
            throw new FieldsAreNotSpecifiedException("Поле name не должно быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.error("Поле name не должно быть пустым");
            throw new FieldsAreNotSpecifiedException("Поле description не должно быть пустым");
        }
        return ItemMapper.toItemDto(itemRepository.createItem(userId, ItemMapper.toItem(itemDto)));
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
        return ItemMapper.toItemDto(itemRepository.updateItem(item));
    }

    @Override
    public ItemDto getItemDtoById(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getItemDtoByUserId(Long userId) {
        userRepository.findUserById(userId);
        return itemRepository.getItemsByUserId(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsDtoByTextRequest(String text) {
        return itemRepository.getItemsByTextRequest(text).stream()
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
            throw new ObjectNotFoundException("Вещь id = " + itemId + "не принадлежит пользователю id = " + userId);
        }
        return item;
    }
}
