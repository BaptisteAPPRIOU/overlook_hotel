package master.master.mapper;

import master.master.domain.Client;
import master.master.web.rest.dto.ClientDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {

  /**
   * Converts a client update DTO into a Client entity.
   */
  Client toEntity(ClientDto.Update dto);

  /**
   * Applies non-null update values to an existing Client entity.
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDto(ClientDto.Update dto, @MappingTarget Client entity);

  /**
   * Converts a Client entity into the DTO returned by the API.
   */
  @Mapping(target = "userId", source = "id")
  // User profile fields are flattened so clients do not need to read nested user data.
  @Mapping(target = "firstName", source = "user.firstName")
  @Mapping(target = "lastName", source = "user.lastName")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "fidelityPoint", source = "fidelityPoints")
  ClientDto.Info toDto(Client entity);
}
