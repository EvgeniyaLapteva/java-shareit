package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    private ItemRequest request;

    private ItemRequestDto requestDto;

    private ItemRequestDtoWithItems requestDtoWithItems;

    @BeforeEach
    void beforeEach() {
        request = new ItemRequest();
        request.setId(1L);
        request.setDescription("need a big knife");

        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("need a big knife")
                .build();
    }

    @Test
    void shouldCreateItemRequestDto() {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        assertEquals(dto.getId(), request.getId());
        assertEquals(dto.getDescription(), request.getDescription());
    }

    @Test
    void shouldCreateItemRequestDtoWithItems() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("knife")
                .description("good thing")
                .available(true)
                .build();

        ItemRequestDtoWithItems dto = ItemRequestMapper.toItemRequestDtoWithItems(request, List.of(itemDto));

        assertEquals(dto.getId(), request.getId());
        assertEquals(dto.getDescription(), request.getDescription());
        assertEquals(dto.getItems().get(0), itemDto);
    }

    @Test
    void shouldCreateItemRequestFromItemRequestDto() {
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto);

        assertEquals(itemRequest.getId(), requestDto.getId());
        assertEquals(itemRequest.getDescription(), requestDto.getDescription());
    }

}