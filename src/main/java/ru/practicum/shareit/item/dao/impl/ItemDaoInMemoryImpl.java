package ru.practicum.shareit.item.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemDaoInMemoryImpl implements ItemDao {

    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> usersItems = new HashMap<>();
    private Long id = 0L;
    @Override
    public Item createItem(Long userId, Item item) {
        item.setId(generateId());
        User owner = User.builder().id(userId).build();
        item.setOwner(owner);
        usersItems.computeIfAbsent(userId, userItems -> new ArrayList<>()).add(item);
//        items.put(item.getId(), item);
//        usersItems.computeIfAbsent(userId, i -> new ArrayList<>()).add(item);
        log.info("Добавлена вещь {}", item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Long userId = item.getOwner().getId();
        usersItems.computeIfPresent(userId, (key, value) -> value.stream()
                .map(itemOfUser -> Objects.equals(itemOfUser.getId(), item.getId()) ? item : itemOfUser)
                .collect(Collectors.toList()));
//        items.put(item.getId(), item);
        log.info("Обновлена вещь {}", item);
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
//        if (!items.containsKey(itemId)) {
//            log.error("Не найдена вещь с id = {}", itemId);
//            throw new ObjectNotFoundException("Не найдена вещь с id = " + itemId);
//        }
//        log.info("Найдена вещь с id = {}", itemId);
//        return items.get(itemId);
        Item item = usersItems.values().stream()
                .flatMap(Collection::stream)
                .filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Не найдена вещь с id = " + itemId);
                    return new ObjectNotFoundException("Не найдена вещь с id = " + itemId);
                });
        log.info("Найдена вещь с id = {}", itemId);
        return item;
    }

    @Override
    public List<Item> getItemsByUserId(Long userId) {
        log.info("Получили список вещей пользователя с id = {}", userId);
        return usersItems.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public List<Item> getItemsByTextRequest(String textRequest) {
        log.info("Получили список вещей по текстовому запросу {}", textRequest);
//        return items.values().stream()
//                .filter(item -> item.isAvailable() && (item.getName().toLowerCase().contains(textRequest))
//                || item.getDescription().toLowerCase().contains(textRequest)).collect(Collectors.toList());
        return usersItems.values().stream()
                .flatMap(Collection::stream)
                .filter(item -> item.isAvailable() && (item.getName().toLowerCase().contains(textRequest))
                || item.getDescription().toLowerCase().contains(textRequest)).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId, Long itemId) {
        if (usersItems.containsKey(userId)) {
            List<Item> items = usersItems.get(userId);
            items.removeIf(item -> item.getId().equals(itemId));
        }
    }

    private Long generateId() {
        return ++id;
    }
}
