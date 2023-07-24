package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.exception.validation.ValidationMarker;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {

    private final ItemClient client;

    @PostMapping
    @Validated(ValidationMarker.OnCreate.class)
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Запрос на создание вещи");
        return client.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("itemId") Long itemId,
                              @RequestBody ItemDto itemDto) {
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                log.error("Поле name не должно быть пустым");
                throw new ValidationException("Поле name не должно быть пустым");
            }
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                log.error("Поле name не должно быть пустым");
                throw new ValidationException("Поле description не должно быть пустым");
            }
        }
        log.info("Запрос на добавление вещи");
        return client.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        log.info("Запрос на получение вещи по id = {}", itemId);
        return client.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                   @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос на получение списка всех вещей пользователя id = {}", userId);
        return client.getItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByTextRequest(@RequestParam String text,
                                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                        @RequestParam(defaultValue = "10") @Positive int size,
                                                        @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на поиск вещи по тексту");
        return client.getItemsByTextRequest(text, from, size, userId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId) {
        log.info("Запрос на удаление вещи по id = {}", itemId);
        return client.deleteItemById(userId, itemId);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody CommentDto commentDto, @PathVariable Long itemId) {
        log.info("Запрос на создание комментария");
        return client.createComment(commentDto, userId, itemId);
    }
}
