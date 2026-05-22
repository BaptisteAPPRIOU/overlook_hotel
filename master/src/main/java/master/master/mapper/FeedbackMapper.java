package master.master.mapper;

import master.master.domain.HotelFeedback;
import master.master.web.rest.dto.FeedbackDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "displayed", constant = "false")
  @Mapping(target = "responseDate", ignore = true)
  @Mapping(target = "responseContent", ignore = true)
  HotelFeedback toEntity(FeedbackDto.Create dto);

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "createdAt", target = "date")
  @Mapping(source = "responseContent", target = "answer")
  FeedbackDto.Info toDto(HotelFeedback entity);
}
