package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.exception.validation.ValidationMarker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotBlank(groups = ValidationMarker.OnCreate.class, message = "Имч не может быть пустым")
    private String name;
    @Email(message = "Некорректный формат почты")
    @NotBlank(groups = ValidationMarker.OnCreate.class, message = "Поле email не может быть пустым")
    private String email;
}
