package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;

import java.util.List;

public interface ItemService {

    ItemDto createItemDto(Long userId, ItemDto itemDto);

    ItemDto updateItemDto(Long userId, Long itemId, ItemDto itemDto);

    ItemDtoWithBookingAndComments getItemDtoById(Long itemId, Long userId);

    List<ItemDtoWithBookingAndComments> getItemDtoByUserId(Long userId);

    List<ItemDto> getItemsDtoByTextRequest(String text);

    void deleteItemDto(Long userId, Long itemId);

    CommentDto createComment(CommentDto commentDto, Long userId, Long ItemId);
}