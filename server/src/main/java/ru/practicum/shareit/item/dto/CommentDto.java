package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    private Long id;

    @NotBlank(message = "Добавьте текст комментария")
    private String text;
    private String authorName;
    private Long itemId;
    private LocalDateTime created;
}
