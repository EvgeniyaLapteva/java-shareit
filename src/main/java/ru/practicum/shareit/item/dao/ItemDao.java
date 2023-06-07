package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemDao {
    Item createItem(Long userId, Item item);
    Item updateItem(Item item);
    Item getItemById(Long itemId);
    List<Item> getItemsByUserId(Long userId);
    List<Item> getItemsByTextRequest(String textRequest);
    void delete(Long userId, Long itemId);
}
