package master.master.mapper;

import master.master.domain.HotelFeedback;
import master.master.web.rest.dto.FeedbackDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

  /**
   * Converts a feedback creation DTO into a HotelFeedback entity.
   */
  @Mapping(target = "id", ignore = true)
  // The service attaches the authenticated user and persistence metadata.
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "displayed", constant = "false")
  @Mapping(target = "responseDate", ignore = true)
  @Mapping(target = "responseContent", ignore = true)
  HotelFeedback toEntity(FeedbackDto.Create dto);

  /**
   * Converts a HotelFeedback entity into the DTO returned by the API.
   */
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "createdAt", target = "date")
  // The API exposes the staff response with the client-facing name "answer".
  @Mapping(source = "responseContent", target = "answer")
  FeedbackDto.Info toDto(HotelFeedback entity);
}
