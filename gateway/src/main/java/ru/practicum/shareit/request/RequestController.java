package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RequestController {

    private final RequestClient client;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Запрос на создание запроса на вещь");
        return client.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestByRequestorId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение списка всех запросов на вещи от пользователя d = {}", userId);
        return client.getRequestsByRequestorId(userId);

    }

    @GetMapping ("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос на получение списка всех запросов на вещи постранично");
        return client.getAllRequestsPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable("requestId") Long requestId) {
        log.info("Запрос на получение запроса на вещи по id = {}", requestId);
        return client.getRequestById(userId, requestId);
    }
}
