package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItemDto(Long userId, ItemDto itemDto);
    ItemDto updateItemDto(Long userId, Long itemId, ItemDto itemDto);
    ItemDto getItemDtoById(Long itemId);
    List<ItemDto> getItemDtoByUserId(Long userId);
    List<ItemDto> getItemsDtoByTextRequest(String text);
    void deleteItemDto(Long userId, Long itemId);
}
