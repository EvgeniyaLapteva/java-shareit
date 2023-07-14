package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = new ItemRequest();
            itemRequest.setId(itemDto.getRequestId());
            item.setRequest(itemRequest);
        }
        return item;
    }

    public static ItemDtoWithBookingAndComments toItemDtoWBC(Item item, List<Comment> comments, List<Booking> bookings,
                                                             User user) {
        List<CommentDto> commentDto = comments.stream()
                .map(CommentMapper::toCommentDto).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED)
                .filter(booking -> booking.getStart().isBefore(now))
                .filter(booking -> booking.getItem().getOwner().getId().equals(user.getId()))
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .max(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED)
                .filter(booking -> booking.getStart().isAfter(now))
                .filter(booking -> booking.getItem().getOwner().getId().equals(user.getId()))
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        return ItemDtoWithBookingAndComments.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .comments(commentDto)
                .lastBooking(lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null)
                .nextBooking(nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null)
                .build();
    }
}
