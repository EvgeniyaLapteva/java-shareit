package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody ItemDto itemDto) {
        return itemService.createItemDto(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("itemId") Long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItemDto(userId, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDtoWithBookingAndComments getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long itemId) {
        log.info("Запрос на получение вещи по id = {}", itemId);
        return itemService.getItemDtoById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoWithBookingAndComments> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @RequestParam int from,
                                                                @RequestParam int size) {
        return itemService.getItemDtoByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByTextRequest(@RequestParam String text,
                                               @RequestParam int from, @RequestParam int size) {
        return itemService.getItemsDtoByTextRequest(text, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long itemId) {
        itemService.deleteItemDto(userId, itemId);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody CommentDto commentDto, @PathVariable Long itemId) {
        return itemService.createComment(commentDto, userId, itemId);
    }
}
