package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.model.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    ItemRequestServiceImpl service;

    private ItemRequest itemRequest;

    private User owner;

    private User requestor;

    private Item item;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@mail.ru");

        requestor = new User();
        requestor.setId(2L);
        requestor.setName("Rick");
        requestor.setEmail("rick@mail.ru");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("need ladder");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        item = new Item();
        item.setId(1L);
        item.setName("ladder");
        item.setDescription("high");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(itemRequest);
    }

    @Test
    void shouldCreateItemRequest() {
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));

        ItemRequestDto responseDto = service.createRequest(requestor.getId(),
                ItemRequestDto.builder().description(itemRequest.getDescription()).build());

        assertNotNull(responseDto);
        assertEquals(itemRequest.getId(), responseDto.getId());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSaveRequestIfUserNotFound() {
        Long userId = 0L;
        String errorMessage = "Пользователь с id = " + userId + " не найден";
        when(userRepository.findById(userId))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.createRequest(userId, ItemRequestDto.builder().description(itemRequest.getDescription())
                        .build())
        );

        assertEquals(errorMessage, exception.getMessage());
        verify(itemRequestRepository, never())
                .save(any());
    }

    @Test
    void shouldFindRequestDtoByRequestorId() {
        Long userId = requestor.getId();
        when(userRepository.findById(requestor.getId()))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorId(userId))
                .thenReturn(List.of(itemRequest));

        List<ItemRequestDtoWithItems> itemRequests = service.getRequestDtoByRequestorId(userId);

        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());
        verify(itemRequestRepository, times(1))
                .findByRequestorId(userId);
    }

    @Test
    void shouldThrowExceptionWhenFindRequestsByRequestorIdIfUserNotFound() {
        Long userId = 0L;
        String errorMessage = "Пользователь с id = " + userId + " не найден";
        when(userRepository.findById(userId))
                .thenThrow(new ObjectNotFoundException(errorMessage));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> service.getRequestDtoByRequestorId(userId)
        );

        assertEquals(errorMessage, exception.getMessage());
        verify(itemRequestRepository, never())
                .findByRequestorId(anyLong());
    }

    @Test
    void shouldGetAllRequestsPageableWhenEmptyRequestsList() {
        Long userId = requestor.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size, SORT);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdNot(userId,page))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDtoWithItems> itemRequestDtos = service.getAllRequestsPageable(userId, from, size);

        assertNotNull(itemRequestDtos);
        assertEquals(0, itemRequestDtos.size());

        verify(itemRequestRepository, times(1))
                .findAllByRequestorIdNot(userId, page);
    }

    @Test
    void shouldGetAllRequestsPageableWhenRequestExist() {
        Long userId = requestor.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size, SORT);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdNot(userId,page))
                .thenReturn(List.of(itemRequest));

        List<ItemRequestDtoWithItems> itemRequestDtos = service.getAllRequestsPageable(userId, from, size);

        assertNotNull(itemRequestDtos);
        assertEquals(1, itemRequestDtos.size());

        verify(itemRequestRepository, times(1))
                .findAllByRequestorIdNot(userId, page);
    }

    @Test
    void shouldFindRequestById() {
        Long userId = requestor.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestor));
        Long requestId = itemRequest.getId();
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(requestId))
                .thenReturn(List.of(item));

        ItemRequestDtoWithItems itemRequestDto = service.getRequestById(userId, requestId);

        assertNotNull(itemRequestDto);
        assertEquals(1, itemRequestDto.getItems().size());
        assertEquals(requestId, itemRequestDto.getId());
        assertEquals(item.getId(), itemRequestDto.getItems().get(0).getId());
        assertEquals(item.getRequest().getId(), requestId);
    }

    @Test
    void shouldThrowExceptionWhenGetRequestByIdIfRequestNotFound() {
        Long userId = requestor.getId();
        Long requestId = 0L;
        String errorMessage = "Запрос по id = " + requestId + " не найден";
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(requestId))
                .thenThrow(new ObjectNotFoundException("Запрос по id = " + requestId + " не найден"));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                        () -> service.getRequestById(userId, requestId)
        );

        assertEquals(errorMessage, exception.getMessage());
    }
}