package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class BookItemRequestDto {

	@NotNull(message = "Не указан идентификатор вещи")
	private long itemId;

	@NotNull(message = "Не указана дата начала бронирования")
	@FutureOrPresent(message = "Дата начала бронирования должна быть в настоящем или будещем времени")
	private LocalDateTime start;

	@NotNull(message = "Не указана дата окончания бронирования")
	@FutureOrPresent(message = "Дата окончания бронирования должна быть в настоящем или будещем времени")
	private LocalDateTime end;
}
