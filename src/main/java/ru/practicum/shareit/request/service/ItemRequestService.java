package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDtoWithItems> getRequestDtoByRequestorId(Long userId);

    List<ItemRequestDtoWithItems> getAllRequestsPageable(Long userId, int from, int size);

    ItemRequestDtoWithItems getRequestById(Long userId, Long requestId);
}
