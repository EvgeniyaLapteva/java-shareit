package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.validation.ValidationMarker;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
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
    @Validated(ValidationMarker.OnCreate.class)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("Запрос на создание вещи");
        return itemService.createItemDto(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("itemId") Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Запрос на добавление вещи");
        return itemService.updateItemDto(userId, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Запрос на получение вещи по id = {}", itemId);
        return itemService.getItemDtoById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение списка всех вещей пользователя id = {}", userId);
        return itemService.getItemDtoByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByTextRequest(@RequestParam String text) {
        log.info("Запрос на поиск вещи по тексту");
        return itemService.getItemsDtoByTextRequest(text);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long itemId) {
        log.info("Запрос на удаление вещи по id = {}", itemId);
        itemService.deleteItemDto(userId, itemId);
    }
}
