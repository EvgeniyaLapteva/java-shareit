package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
public class ItemRequestDto {

    private Long id;

    @NotBlank(message = "Описание запроса вещи не должно быть пустым")
    private String description;

    private User requestor;

    private LocalDateTime created;

}
