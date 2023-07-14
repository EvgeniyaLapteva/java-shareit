package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void shouldCreateItemFromItemDto() {
        User owner = new User();
        owner.setId(1L);
        owner.setName("Tom");
        owner.setEmail("tom@mail.ru");

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("tool")
                .description("nice")
                .available(true)
                .requestId(1L)
                .build();

        Item item = ItemMapper.toItem(dto, owner);

        assertNotNull(item);
        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getRequestId(), item.getRequest().getId());
    }

}