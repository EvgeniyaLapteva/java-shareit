package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Запрос на создание запроса на вещь");
        return itemRequestService.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDtoWithItems> getRequestDtoByRequestorId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение списка всех запросов на вещи от пользователя d = {}", userId);
        return itemRequestService.getRequestDtoByRequestorId(userId);
    }

    @GetMapping ("/all")
    public List<ItemRequestDtoWithItems> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam(defaultValue = "0", required = false)
                                                        @Min(0) int from, @RequestParam(defaultValue = "10",
                                                        required = false) @Positive int size) {
        log.info("Запрос на получение списка всех запросов на вещи постранично");
        return itemRequestService.getAllRequestsPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItems getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable("requestId") Long requestId) {
        log.info("Запрос на получение запроса на вещт по id = {}", requestId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
