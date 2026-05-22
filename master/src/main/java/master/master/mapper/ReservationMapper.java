package master.master.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import master.master.domain.Reservation;
import master.master.web.rest.dto.ReservationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "room", ignore = true)
  @Mapping(target = "startDatetime", expression = "java(toStartDateTime(dto.getReservationDateStart()))")
  @Mapping(target = "endDatetime", expression = "java(toEndDateTime(dto.getReservationDateEnd()))")
  @Mapping(target = "reservationStatus", ignore = true)
  @Mapping(target = "totalAmount", ignore = true)
  @Mapping(target = "paymentMethod", ignore = true)
  @Mapping(target = "paid", constant = "false")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "review", ignore = true)
  Reservation toEntity(ReservationDto.Create dto);

  @Mapping(source = "client.id", target = "userId")
  @Mapping(source = "room.id", target = "roomId")
  @Mapping(target = "reservationDateStart", expression = "java(toDate(entity.getStartDatetime()))")
  @Mapping(target = "reservationDateEnd", expression = "java(toDate(entity.getEndDatetime()))")
  @Mapping(source = "paid", target = "payed")
  ReservationDto.Info toDto(Reservation entity);

  default LocalDateTime toStartDateTime(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  default LocalDateTime toEndDateTime(LocalDate date) {
    return date == null ? null : date.atTime(23, 59, 59);
  }

  default LocalDate toDate(LocalDateTime dateTime) {
    return dateTime == null ? null : dateTime.toLocalDate();
  }
}
