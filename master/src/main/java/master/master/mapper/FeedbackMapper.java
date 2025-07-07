package master.master.mapper;

import master.master.domain.Feedback;
import master.master.web.rest.dto.FeedbackDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "date", ignore = true)
    Feedback toEntity(FeedbackDto.Create dto);

    @Mapping(source = "user.id", target = "userId")
    FeedbackDto.Info toDto(Feedback entity);
}
