package master.master.mapper;

import master.master.domain.UserReservation;
import master.master.web.rest.dto.ReservationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "room", ignore = true)
    UserReservation toEntity(ReservationDto.Create dto);

    @Mapping(source = "id.userId", target = "userId")
    @Mapping(source = "id.roomId", target = "roomId")
    @Mapping(source = "reservationDateStart", target = "reservationDateStart")
    @Mapping(source = "reservationDateEnd", target = "reservationDateEnd")
    @Mapping(source = "payed", target = "payed")
    ReservationDto.Info toDto(UserReservation entity);
}
