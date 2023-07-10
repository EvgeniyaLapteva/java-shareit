package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = validateUser(userId);
        itemRequestDto.setRequestor(requestor);
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequestRepository.save(itemRequest);
        log.info("Добавлен запрос на вещь: {}", itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDtoWithItems> getRequestDtoByRequestorId(Long userId) {
        validateUser(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId(userId);
        log.info("Нашли все запросы на вещи пользователя id = {}", userId);
        return addItemsToRequest(itemRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDtoWithItems> getAllRequestsPageable(Long userId, int from, int size) {
        validateUser(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        PageRequest page = PageRequest.of(from / size, size, sort);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdNot(userId, page);
        log.info("Получили список всех запросов на вещи");
        return addItemsToRequest(itemRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDtoWithItems getRequestById(Long userId, Long requestId) {
        validateUser(userId);
        ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException("Запрос по id = " + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequestId(requestId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
        log.info("Получен запрос на вещь по id = " + requestId);
        return ItemRequestMapper.toItemRequestDtoWithItems(request, itemDtos);
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь" +
                        "с id = " + userId + " не найден"));
    }

    private List<ItemRequestDtoWithItems> addItemsToRequest(List<ItemRequest> itemRequests) {
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        return itemRequests.stream()
                .map(itemRequest -> {
                    List<ItemDto> itemDtos = items.stream()
                            .map(ItemMapper :: toItemDto)
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestDtoWithItems(itemRequest, itemDtos);
                })
                .collect(Collectors.toList());
    }
}
